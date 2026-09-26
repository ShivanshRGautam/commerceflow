package com.shivansh.commerceflow.order;

import com.shivansh.commerceflow.order.dto.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal Jwt jwt
    ) {
        OrderResponse response =
                orderService.checkout(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                orderService.getMyOrders(
                        jwt.getSubject()
                )
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                orderService.getMyOrder(
                        jwt.getSubject(),
                        orderId
                )
        );
    }
}
