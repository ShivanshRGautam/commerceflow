package com.shivansh.commerceflow.cart;

import com.shivansh.commerceflow.cart.dto.AddCartItemRequest;
import com.shivansh.commerceflow.cart.dto.CartResponse;
import com.shivansh.commerceflow.cart.dto.UpdateCartItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                cartService.getCart(jwt.getSubject())
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartService.addItem(
                        jwt.getSubject(),
                        request
                ));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateItem(
                        jwt.getSubject(),
                        productId,
                        request
                )
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                cartService.removeItem(
                        jwt.getSubject(),
                        productId
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal Jwt jwt
    ) {
        cartService.clearCart(jwt.getSubject());

        return ResponseEntity.noContent().build();
    }
}
