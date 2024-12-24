package com.bubnov.v5.controller;

import com.bubnov.v5.model.ExchangeRate;
import com.bubnov.v5.service.ExchangeRateService;
import com.bubnov.v5.service.ExchangeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeService exchangeService;
    private final Map<String, String> response = new HashMap<>();


    @GetMapping("/rate")
    public ResponseEntity<?> getExchangeRate(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
        if (fromCurrency == null || fromCurrency.trim().isEmpty() || toCurrency == null || toCurrency.trim().isEmpty()){
            response.put("message", "Currency code cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }
        BigDecimal rate = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/max-min")
    public ResponseEntity<?> getMaxMinExchangeRate(
            @RequestParam String fromCurrency,
            @RequestParam String period) {
        if (fromCurrency == null || fromCurrency.trim().isEmpty() || period == null || period.trim().isEmpty()){
            response.put("message", "Currency code and period cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }
        fromCurrency = fromCurrency.toUpperCase();
        Map<String, BigDecimal> exchangeRates = exchangeRateService.getMaxMinRate(fromCurrency, "UAH", period);
        return ResponseEntity.ok(exchangeRates);
    }

    @PostMapping("/set-rate")
    public ResponseEntity<?> setExchangeRate(
            @RequestParam String fromCurrency,
            @RequestParam BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            response.put("message", "Exchange rate must be greater than 0.");
            return ResponseEntity.badRequest().body(response);
        }
        if (fromCurrency == null || fromCurrency.trim().isEmpty()) {
            response.put("message", "Currency code cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }
        fromCurrency = fromCurrency.toUpperCase();
        ExchangeRate exchangeRate = exchangeRateService.addExchangeRate(fromCurrency, "UAH", rate);
        exchangeService.pendingRequestsAll();
        return ResponseEntity.ok(exchangeRate);
    }
}
