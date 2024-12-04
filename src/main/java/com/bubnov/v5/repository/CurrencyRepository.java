package com.bubnov.v5.repository;

import com.bubnov.v5.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    boolean existsByCode(String code);
    void deleteCurrencyByCode(String code);
    Currency findCurrenciesByCode(String code);

}
