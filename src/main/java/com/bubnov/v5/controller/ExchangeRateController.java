package com.bubnov.v5.controller;

import com.bubnov.v5.model.ExchangeRate;
import com.bubnov.v5.service.ExchangeRateService;
import com.bubnov.v5.service.ExchangeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {
    private ExchangeRateService exchangeRateService;
    private ExchangeService exchangeService;

    @GetMapping("/rate")
    public ResponseEntity<BigDecimal> getExchangeRate(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
        BigDecimal rate = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/max-min")
    public ResponseEntity<Map<String, BigDecimal>> getMaxMinExchangeRate(
            @RequestParam String fromCurrency,
            @RequestParam String period) {
        fromCurrency = fromCurrency.toUpperCase();
        Map<String, BigDecimal> exchangeRates = exchangeRateService.getMaxMinRate(fromCurrency, "UAH", period);
        return ResponseEntity.ok(exchangeRates);
    }

    @PostMapping("/set-rate")
    public ResponseEntity<ExchangeRate> setExchangeRate(
            @RequestParam String fromCurrency,
            @RequestParam BigDecimal rate) {
        fromCurrency = fromCurrency.toUpperCase();
        ExchangeRate exchangeRate = exchangeRateService.addExchangeRate(fromCurrency, "UAH", rate);
        exchangeService.pendingRequestsAll();
        return ResponseEntity.ok(exchangeRate);
    }
}
