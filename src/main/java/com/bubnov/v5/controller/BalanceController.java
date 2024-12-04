package com.bubnov.v5.controller;

import com.bubnov.v5.service.ExchangeRateService;
import com.bubnov.v5.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balances")
public class BalanceController {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeService exchangeService;

    @GetMapping
    public Map<String, BigDecimal> getBalances() {
        return exchangeRateService.getBalances();
    }

    @PostMapping
    public void addBalance(@RequestParam String currencyCode, @RequestParam BigDecimal amount) {
        currencyCode = currencyCode.toUpperCase();
        exchangeRateService.addBalance(currencyCode, amount);
        exchangeService.processPendingRequests();
    }
}
