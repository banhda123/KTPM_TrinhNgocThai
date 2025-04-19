package com.ecommerce.payment.service;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentRequest;
import com.ecommerce.payment.model.PaymentResponse;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @RateLimiter(name = "paymentService")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            // In a real system, this would integrate with a payment gateway
            // Simulate payment processing
            Thread.sleep(500); // Simulating external payment gateway call
            
            String transactionId = UUID.randomUUID().toString();
            
            Payment payment = new Payment();
            payment.setOrderId(paymentRequest.getOrderId());
            payment.setAmount(paymentRequest.getAmount());
            payment.setPaymentMethod(paymentRequest.getPaymentMethod());
            payment.setTransactionId(transactionId);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus(PaymentStatus.COMPLETED);
            
            payment = paymentRepository.save(payment);
            
            // Notify other services about the payment
            kafkaTemplate.send("payment-events", "Payment processed: " + payment.getId());
            
            return mapToPaymentResponse(payment, "Payment processed successfully");
        } catch (Exception e) {
            Payment payment = new Payment();
            payment.setOrderId(paymentRequest.getOrderId());
            payment.setAmount(paymentRequest.getAmount());
            payment.setPaymentMethod(paymentRequest.getPaymentMethod());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus(PaymentStatus.FAILED);
            
            payment = paymentRepository.save(payment);
            
            return mapToPaymentResponse(payment, "Payment processing failed: " + e.getMessage());
        }
    }

    public PaymentResponse processPaymentFallback(PaymentRequest paymentRequest, Exception e) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(paymentRequest.getOrderId());
        response.setAmount(paymentRequest.getAmount());
        response.setPaymentMethod(paymentRequest.getPaymentMethod());
        response.setStatus(PaymentStatus.FAILED);
        response.setMessage("Payment service is currently unavailable. Please try again later.");
        return response;
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getPaymentByIdFallback")
    @RateLimiter(name = "paymentService")
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        return mapToPaymentResponse(payment, "Payment retrieved successfully");
    }

    public PaymentResponse getPaymentByIdFallback(Long paymentId, Exception e) {
        PaymentResponse response = new PaymentResponse();
        response.setId(paymentId);
        response.setMessage("Payment service is currently unavailable. Please try again later.");
        return response;
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getPaymentsByOrderIdFallback")
    @RateLimiter(name = "paymentService")
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(payment -> mapToPaymentResponse(payment, "Payment retrieved successfully"))
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByOrderIdFallback(Long orderId, Exception e) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setMessage("Payment service is currently unavailable. Please try again later.");
        return List.of(response);
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "refundPaymentFallback")
    @RateLimiter(name = "paymentService")
    @Retry(name = "paymentService")
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            return mapToPaymentResponse(payment, "Cannot refund payment that is not completed");
        }
        
        // In a real system, this would integrate with a payment gateway for refund
        // Simulate refund processing
        try {
            Thread.sleep(500); // Simulating external payment gateway call
            
            payment.setStatus(PaymentStatus.REFUNDED);
            payment = paymentRepository.save(payment);
            
            // Notify other services about the refund
            kafkaTemplate.send("payment-events", "Payment refunded: " + payment.getId());
            
            return mapToPaymentResponse(payment, "Payment refunded successfully");
        } catch (Exception e) {
            return mapToPaymentResponse(payment, "Refund processing failed: " + e.getMessage());
        }
    }

    public PaymentResponse refundPaymentFallback(Long paymentId, Exception e) {
        PaymentResponse response = new PaymentResponse();
        response.setId(paymentId);
        response.setStatus(PaymentStatus.FAILED);
        response.setMessage("Refund service is currently unavailable. Please try again later.");
        return response;
    }

    private PaymentResponse mapToPaymentResponse(Payment payment, String message) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setPaymentDate(payment.getPaymentDate());
        response.setStatus(payment.getStatus());
        response.setMessage(message);
        return response;
    }
} 