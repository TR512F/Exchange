package com.bubnov.v5.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRequestDto {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
}
