package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    boolean existsByTransactionId(String transactionId);
}
