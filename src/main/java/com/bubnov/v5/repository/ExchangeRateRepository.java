package com.bubnov.v5.repository;

import com.bubnov.v5.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("SELECT MAX(er.rate) FROM ExchangeRate er WHERE er.fromCurrency.code = :fromCurrency AND er.toCurrency.code = :toCurrency AND er.timestamp >= :startDate")
    Optional<BigDecimal> findMaxRate(@Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT MIN(er.rate) FROM ExchangeRate er WHERE er.fromCurrency.code = :fromCurrency AND er.toCurrency.code = :toCurrency AND er.timestamp >= :startDate")
    Optional<BigDecimal> findMinRate(@Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT e.rate FROM ExchangeRate e WHERE e.fromCurrency.code = :fromCurrencyCode AND e.toCurrency.code = :toCurrencyCode ORDER BY e.timestamp DESC LIMIT 1")
    Optional<BigDecimal> findLatestRate(String fromCurrencyCode, String toCurrencyCode);
}