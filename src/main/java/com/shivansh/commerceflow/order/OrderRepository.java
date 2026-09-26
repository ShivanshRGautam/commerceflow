package com.shivansh.commerceflow.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<CustomerOrder> findByIdAndUserId(
            Long orderId,
            Long userId
    );
}
