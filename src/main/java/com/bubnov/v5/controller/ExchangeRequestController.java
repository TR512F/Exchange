package com.bubnov.v5.controller;

import com.bubnov.v5.model.ExchangeRequest;
import com.bubnov.v5.model.dto.ExchangeRequestDto;
import com.bubnov.v5.service.ExchangeService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/exchange")
@RestController
public class ExchangeRequestController {
    @Autowired
    private ExchangeService exchangeService;

    @PostMapping("/create")
    public ResponseEntity<ExchangeRequest> createExchangeRequest(@RequestBody ExchangeRequestDto requestDto) {
        ExchangeRequest exchangeRequest = exchangeService.createExchangeRequest(
                requestDto.getFromCurrency(),
                requestDto.getToCurrency(),
                requestDto.getAmount()
        );
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