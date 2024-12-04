package com.bubnov.v5.service;

import com.bubnov.v5.model.*;
import com.bubnov.v5.repository.CurrencyRepository;
import com.bubnov.v5.repository.ExchangeRateRepository;
import com.bubnov.v5.repository.ExchangeRequestRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Data
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, BigDecimal> balances = new HashMap<>();

    @Autowired
    public ExchangeRateService(ExchangeRequestRepository exchangeRequestRepository,
                               ExchangeRateRepository exchangeRateRepository,
                               CurrencyRepository currencyRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
        this.messagingTemplate = messagingTemplate;

        currencyRepository.findAll().forEach(currency ->
                balances.put(currency.getCode(), BigDecimal.valueOf(1000))
        );
    }

    public BigDecimal getExchangeRate(String reqFromCurrencyCode, String reqFroCurrencyCode) {
        String fromCurrencyCode = reqFromCurrencyCode.toUpperCase();
        String toCurrencyCode = reqFroCurrencyCode.toUpperCase();
    if (fromCurrencyCode.equalsIgnoreCase(toCurrencyCode)) {
            return BigDecimal.ONE;
        }

        Optional<BigDecimal> directRate = findDirectRate(fromCurrencyCode, toCurrencyCode);
        if (directRate.isPresent()) {
            return directRate.get();
        }

        Optional<BigDecimal> inverseRate = findDirectRate(toCurrencyCode, fromCurrencyCode);
        if (inverseRate.isPresent()) {
            return BigDecimal.ONE.divide(inverseRate.get(), 2, BigDecimal.ROUND_HALF_UP);
        }

        return calculateCrossRate(fromCurrencyCode, toCurrencyCode);
    }

    private Optional<BigDecimal> findDirectRate(String fromCurrencyCode, String toCurrencyCode) {
        return exchangeRateRepository.findLatestRate(fromCurrencyCode, toCurrencyCode);
    }

    private BigDecimal calculateCrossRate(String fromCurrencyCode, String toCurrencyCode) {
        BigDecimal fromToUahRate = findDirectRate(fromCurrencyCode, "UAH")
                .orElseGet(() -> findDirectRate("UAH", fromCurrencyCode)
                        .map(rate -> BigDecimal.ONE.divide(rate, 2, BigDecimal.ROUND_HALF_UP))
                        .orElseThrow(() -> new IllegalArgumentException("Exchange rate not available between " + fromCurrencyCode + " and UAH")));

        BigDecimal uahToToRate = findDirectRate("UAH", toCurrencyCode)
                .orElseGet(() -> findDirectRate(toCurrencyCode, "UAH")
                        .map(rate -> BigDecimal.ONE.divide(rate, 2, BigDecimal.ROUND_HALF_UP))
                        .orElseThrow(() -> new IllegalArgumentException("Exchange rate not available between UAH and " + toCurrencyCode)));

        return fromToUahRate.multiply(uahToToRate);
    }

    @Transactional
    public ExchangeRate addExchangeRate(String reqFromCurrencyCode, String reqToCurrencyCode, BigDecimal rate) {
        String fromCurrencyCode = reqFromCurrencyCode.toUpperCase();
        String toCurrencyCode = reqToCurrencyCode.toUpperCase();
        if (currencyRepository.existsByCode(fromCurrencyCode) && currencyRepository.existsByCode(toCurrencyCode) && rate != null) {
            ExchangeRate newExchangeRate = new ExchangeRate();
            newExchangeRate.setFromCurrency(currencyRepository.findCurrenciesByCode(fromCurrencyCode));
            newExchangeRate.setToCurrency(currencyRepository.findCurrenciesByCode(toCurrencyCode));
            newExchangeRate.setRate(rate);
            newExchangeRate.setTimestamp(LocalDateTime.now());

            ExchangeRate savedExchangeRate = exchangeRateRepository.save(newExchangeRate);

            Map<String, BigDecimal> latestRates = getCurrentExchangeRatesToUAH();
            messagingTemplate.convertAndSend("/topic/exchange-rates", latestRates);
            return savedExchangeRate;

        }else throw new RuntimeException("Currency not found");
    }

    @Transactional
    public void removeExchangeRate(String reqFromCurrencyCode) {
        String currencyCode = reqFromCurrencyCode.toUpperCase();
        if (currencyRepository.existsByCode(currencyCode)) {
            currencyRepository.deleteCurrencyByCode(currencyCode);

            Map<String, BigDecimal> latestRates = getCurrentExchangeRatesToUAH();
            messagingTemplate.convertAndSend("/topic/exchange-rates", latestRates);
        }else throw new RuntimeException("Currency not found");
    }

    public Map<String, BigDecimal> getCurrentExchangeRatesToUAH() {
        Map<String, BigDecimal> exchangeRates = new HashMap<>();
        List<Currency> currencies = currencyRepository.findAll();

        for (Currency currency : currencies) {
            if (!currency.getCode().equals("UAH")) {
                Optional<BigDecimal> rate = exchangeRateRepository.findLatestRate(currency.getCode(), "UAH");
                rate.ifPresent(value -> exchangeRates.put(currency.getCode() + " -> UAH", value));
            }
        }
        return exchangeRates;
    }

    public Map<String, BigDecimal> getMaxMinRate(String fromCurrencyCode, String toCurrencyCode, String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period.toLowerCase()) {
            case "day" -> startDate = now.minusDays(1);
            case "week" -> startDate = now.minusWeeks(1);
            case "month" -> startDate = now.minusMonths(1);
            default -> throw new IllegalArgumentException("Unsupported period: " + period);
        }

        Optional<BigDecimal> maxRateOpt = exchangeRateRepository.findMaxRate(fromCurrencyCode, toCurrencyCode, startDate);
        Optional<BigDecimal> minRateOpt = exchangeRateRepository.findMinRate(fromCurrencyCode, toCurrencyCode, startDate);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("maxRate", maxRateOpt.orElseThrow(() -> new RuntimeException("No max rate data found for the given currency pair and period.")));
        result.put("minRate", minRateOpt.orElseThrow(() -> new RuntimeException("No min rate data found for the given currency pair and period.")));
        return result;
    }

    public boolean isReserveSufficient(String currency, BigDecimal requiredAmount) {
        return balances.getOrDefault(currency, BigDecimal.ZERO).compareTo(requiredAmount) >= 0;
    }

    public void deductBalance(String currency, BigDecimal amount) {
        balances.put(currency, balances.get(currency).subtract(amount));
        messagingTemplate.convertAndSend("/topic/balances", balances);
    }

       public void addBalance(String currency, BigDecimal amount) {
        balances.put(currency, balances.get(currency).add(amount));
        messagingTemplate.convertAndSend("/topic/balances", balances);
    }

    public void addCurrencyToBalances(String currencyCode) {
        balances.put(currencyCode, BigDecimal.valueOf(0));
        messagingTemplate.convertAndSend("/topic/balances", balances);
    }

    public void delCurrencyFromBalances(String currencyCode) {
        balances.remove(currencyCode);
        messagingTemplate.convertAndSend("/topic/balances", balances);
    }
}
