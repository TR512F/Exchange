package com.bubnov.v5.repository;
import com.bubnov.v5.model.ExchangeRequest;
import com.bubnov.v5.model.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findByStatus(ExchangeStatus status);
//    List<ExchangeRequest> findByStatusAndUserId(ExchangeStatus status, String userId);
    List<ExchangeRequest> findByStatusAndToCurrency(ExchangeStatus status, String toCurrency);
}
