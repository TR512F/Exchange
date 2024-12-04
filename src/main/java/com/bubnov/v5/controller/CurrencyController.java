package com.bubnov.v5.controller;

import com.bubnov.v5.model.Currency;
import com.bubnov.v5.repository.CurrencyRepository;
import com.bubnov.v5.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Currency> addCurrency(@RequestParam String currencyCode){
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
