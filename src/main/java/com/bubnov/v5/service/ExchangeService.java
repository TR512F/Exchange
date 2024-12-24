package com.bubnov.v5.service;

import com.bubnov.v5.model.Currency;
import com.bubnov.v5.model.ExchangeRequest;
import com.bubnov.v5.model.ExchangeStatus;
import com.bubnov.v5.repository.ExchangeRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SimpUserRegistry simpUserRegistry;
    private final WebSocketService webSocketService;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public ExchangeRequest createExchangeRequest(String fromCurrency, String toCurrency, BigDecimal amount) {
        fromCurrency = fromCurrency.toUpperCase();
        toCurrency = toCurrency.toUpperCase();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        BigDecimal currentRate = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
        BigDecimal requiredAmount = amount.multiply(currentRate);

        ExchangeRequest request = new ExchangeRequest();
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(amount);
        request.setRateAtRequestTime(currentRate);
        request.setStatus(ExchangeStatus.PENDING);
        request.setUserId(userId);
        request.setCreatedAt(LocalDateTime.now());

        if (exchangeRateService.isReserveSufficient(toCurrency, requiredAmount)) {
            request.setStatus(ExchangeStatus.COMPLETED);
            exchangeRateService.deductBalance(toCurrency, requiredAmount);
        }

        return exchangeRequestRepository.save(request);
    }

    public void pendingRequestsAll() {
        List<ExchangeRequest> pendingRequests = exchangeRequestRepository.findByStatus(ExchangeStatus.PENDING);
        if (!pendingRequests.isEmpty()) {
            checkPendingRequests(pendingRequests);
        }
    }

//    public void pendingRequestsByUser(String userId) {
//        List<ExchangeRequest> pendingRequests = exchangeRequestRepository.findByStatusAndUserId(ExchangeStatus.PENDING,
//                userId);
//        if (!pendingRequests.isEmpty()) {
//            checkPendingRequests(pendingRequests);
//        }
//    }

    public void pendingRequestsByCurrency(String currencyCode) {
        List<ExchangeRequest> pendingRequests = exchangeRequestRepository.findByStatusAndToCurrency(
                ExchangeStatus.PENDING,
                currencyCode
        );
        if (!pendingRequests.isEmpty()) {
            checkPendingRequests(pendingRequests);
        }
    }

    public void checkPendingRequests(List<ExchangeRequest> pendingRequests) {

        for (ExchangeRequest request : pendingRequests) {

            if (simpUserRegistry.getUser(request.getUserId()) != null) {
                BigDecimal currentRate = exchangeRateService.getExchangeRate(request.getFromCurrency(), request.getToCurrency())
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal requiredAmount = request.getAmount().multiply(currentRate);

                if (exchangeRateService.isReserveSufficient(request.getToCurrency(), requiredAmount)) {

                    if (currentRate.equals(request.getRateAtRequestTime())) {
                        completeRequest(request, requiredAmount);
                    } else {
                        notifyUserForRateChange(request, currentRate);
                    }
                }
            }
        }
    }

    private void completeRequest(ExchangeRequest request, BigDecimal requiredAmount) {
        if (request.getStatus() == ExchangeStatus.PENDING) {
            request.setStatus(ExchangeStatus.COMPLETED);
            exchangeRateService.deductBalance(request.getToCurrency(), requiredAmount);
            exchangeRequestRepository.save(request);
            webSocketService.sendMessageToUser(request.getUserId(),
                    "Your exchange request №" + request.getId() + " has been completed at the requested rate.");
        }
    }

    private void notifyUserForRateChange(ExchangeRequest request, BigDecimal newRate) {
        webSocketService.sendMessageToUser(request.getUserId(),
                "Rate changed for your exchange request №" + request.getId() + ". New rate: " + newRate +
                        ". Please confirm or reject the request.");
    }

    public void confirmExchangeRequest(Long requestId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        BigDecimal currentRate = exchangeRateService.getExchangeRate(request.getFromCurrency(), request.getToCurrency());
        BigDecimal requiredAmount = request.getAmount().multiply(currentRate);

        if (exchangeRateService.isReserveSufficient(request.getToCurrency(), requiredAmount)) {
            completeRequest(request, requiredAmount);
        } else {
            throw new IllegalStateException("Insufficient funds for completing the request.");
        }
    }

    public void rejectExchangeRequest(Long requestId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (request.getStatus() == ExchangeStatus.PENDING) {
            request.setStatus(ExchangeStatus.REJECTED);
            exchangeRequestRepository.save(request);
            webSocketService.sendMessageToUser(request.getUserId(),
                    "Your exchange request №" + request.getId() + " has been rejected.");
        }
    }
}
