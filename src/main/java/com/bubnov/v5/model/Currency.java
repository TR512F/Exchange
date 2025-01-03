package com.bubnov.v5.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "currencies")
public class Currency {
    public Currency(String code) {
        this.code = code;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    @NotBlank(message = "CurrencyCode cannot be blank")
    private String code;
}