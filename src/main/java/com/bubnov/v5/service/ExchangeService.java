package com.bubnov.v5.service;

import com.bubnov.v5.model.ExchangeRequest;
import com.bubnov.v5.model.ExchangeStatus;
import com.bubnov.v5.repository.ExchangeRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExchangeService {
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeService(ExchangeRequestRepository exchangeRequestRepository,
                           SimpMessagingTemplate messagingTemplate,
                           ExchangeRateService exchangeRateService) {
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.messagingTemplate = messagingTemplate;
        this.exchangeRateService = exchangeRateService;
    }

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
        } else {
            messagingTemplate.convertAndSend("/topic/messages", "Your exchange request is pending due to insufficient funds.");

//TODO            messagingTemplate.convertAndSendToUser(
//                    userId,
//                    "/queue/notifications",
//                    "USER!! Your exchange request is pending due to insufficient funds."
//            );
        }

        return exchangeRequestRepository.save(request);
    }

    public void processPendingRequests() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName(); // Получение текущего пользователя
        List<ExchangeRequest> pendingRequests = exchangeRequestRepository.findByStatusAndUserId(
                ExchangeStatus.PENDING,
                userId
        );
        System.out.println("\n!!Panding!! " + pendingRequests);

        for (ExchangeRequest request : pendingRequests) {
            System.out.println("Зашли в pendingRequests");
            BigDecimal currentRate = exchangeRateService.getExchangeRate(request.getFromCurrency(), request.getToCurrency()); //текущий(новый)
            System.out.println("\n!! currentRate: " + currentRate);
            BigDecimal requiredAmount = request.getAmount().multiply(currentRate);//количество денег
            System.out.println("\n!! requiredAmount: " + requiredAmount);


            if (exchangeRateService.isReserveSufficient(request.getToCurrency(), requiredAmount)) {
                System.out.println("\n!! Rates!!: currentRate: " + currentRate + " | " + request.getRateAtRequestTime());
                if (currentRate.equals(request.getRateAtRequestTime())) {
                    completeRequest(request, requiredAmount);
                    System.out.println("\n!! complete!");
                } else {
                    System.out.println("\n!! oops!");
                    notifyUserForRateChange(request, currentRate);
                }
            }
        }
    }

    private void completeRequest(ExchangeRequest request, BigDecimal requiredAmount) {
        if (request.getStatus() == ExchangeStatus.PENDING) {
            request.setStatus(ExchangeStatus.COMPLETED);
            exchangeRateService.deductBalance(request.getToCurrency(), requiredAmount);
            exchangeRequestRepository.save(request);

            messagingTemplate.convertAndSend("/topic/messages", "Your exchange request has been completed at the requested rate.");

//TODO        messagingTemplate.convertAndSendToUser(
//                request.getUserId(),
//                "/queue/notifications",
//                "Your exchange request has been completed at the requested rate."
//        );
        }
    }

    private void notifyUserForRateChange(ExchangeRequest request, BigDecimal newRate) {
        messagingTemplate.convertAndSend("/topic/messages", "Rate changed for your exchange request №" + request.getId() + ". New rate: " + newRate +
                ". Please confirm or reject the request.");

//TODO        messagingTemplate.convertAndSendToUser(
//                request.getUserId(),
//                "/queue/notifications",
//                "Rate changed for your exchange request. New rate: " + newRate +
//                        ". Please confirm or reject the request."
//        );
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
            messagingTemplate.convertAndSendToUser(
                    request.getUserId(),
                    "/queue/notifications",
                    "Your exchange request has been rejected."
            );
        }
    }
}