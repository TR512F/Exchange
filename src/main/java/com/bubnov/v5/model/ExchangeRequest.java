package com.bubnov.v5.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class ExchangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal rateAtRequestTime;

    @Enumerated(EnumType.STRING)
    private ExchangeStatus status;

    private LocalDateTime createdAt;
    private String userId;
}

