package com.bubnov.v5.controller;

import com.bubnov.v5.service.ExchangeRateService;
import com.bubnov.v5.service.ExchangeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/balances")
public class BalanceController {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeService exchangeService;

    @GetMapping
    public Map<String, BigDecimal> getBalances() {
        return exchangeRateService.getBalances();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addBalance(@RequestParam String currencyCode, @RequestParam BigDecimal amount) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Currency code cannot be empty."));
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount must be greater than zero."));
        }
        currencyCode = currencyCode.toUpperCase();
        exchangeRateService.addBalance(currencyCode, amount);
        exchangeService.pendingRequestsByCurrency(currencyCode);
        return ResponseEntity.ok(Map.of("message", "Balance updated successfully."));
    }
}
