package com.bubnov.v5.controller;

import com.bubnov.v5.model.Currency;
import com.bubnov.v5.repository.CurrencyRepository;
import com.bubnov.v5.service.ExchangeRateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/currencies")
public class CurrencyController {
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> addCurrency(@RequestParam String currencyCode){
        if (currencyCode == null || currencyCode.trim().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("message", "Currency code cannot be empty."));
        }
        currencyCode = currencyCode.toUpperCase();
        if (currencyRepository.existsByCode(currencyCode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        Currency savedCurrency = currencyRepository.save(new Currency(currencyCode));
        exchangeRateService.addCurrencyToBalances(currencyCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCurrency);
    }

    @DeleteMapping()
    public void deleteCurrency(@RequestParam String currencyCode) {
        currencyCode = currencyCode.toUpperCase();
        exchangeRateService.removeExchangeRate(currencyCode);
        exchangeRateService.delCurrencyFromBalances(currencyCode);
    }
}
