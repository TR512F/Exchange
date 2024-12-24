package com.bubnov.v5.controller;

import com.bubnov.v5.model.ExchangeRequest;
import com.bubnov.v5.model.dto.ExchangeRequestDto;
import com.bubnov.v5.service.ExchangeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/exchange")
@RestController
@AllArgsConstructor
public class ExchangeRequestController {
    private final ExchangeService exchangeService;

    @PostMapping("/create")
    public ResponseEntity<?> createExchangeRequest(@RequestBody @Valid ExchangeRequestDto requestDto) {
        String fromCurrency = requestDto.getFromCurrency().toUpperCase();
        String toCurrency = requestDto.getToCurrency().toUpperCase();
        if (fromCurrency.equals(toCurrency)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Currencies cannot be the same."));
        }
        ExchangeRequest exchangeRequest = exchangeService
                .createExchangeRequest(fromCurrency, toCurrency, requestDto.getAmount());
        return ResponseEntity.ok(exchangeRequest);
    }

    @PostMapping("/approve")
    public void createExchangeRequest(@RequestParam @NonNull Long reqId, Boolean isApproved) {
        if (!isApproved) {
            exchangeService.rejectExchangeRequest(reqId);
        } else {
            exchangeService.confirmExchangeRequest(reqId);
        }
    }
}