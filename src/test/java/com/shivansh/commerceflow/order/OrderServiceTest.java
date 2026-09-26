package com.shivansh.commerceflow.order;

import com.shivansh.commerceflow.cart.Cart;
import com.shivansh.commerceflow.cart.CartItemRepository;
import com.shivansh.commerceflow.cart.CartRepository;
import com.shivansh.commerceflow.inventory.InventoryRepository;
import com.shivansh.commerceflow.user.AppUser;
import com.shivansh.commerceflow.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkout_ShouldThrowException_WhenCartIsEmpty() {
        // 1. Arrange: Setup our fake data and mock repository responses
        AppUser mockUser = new AppUser();
        mockUser.setId(1L);
        mockUser.setEmail("shivansh@gmail.com");

        Cart mockCart = new Cart();
        mockCart.setId(1L);

        // When the service looks up the user, return our mock user
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(mockUser));

        // When the service looks up the cart, return our mock cart
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(mockCart));

        // When the service looks up cart items, return an empty list!
        when(cartItemRepository.findByCartIdOrderByCreatedAtAsc(anyLong())).thenReturn(Collections.emptyList());

        // 2. Act & Assert: Execute the checkout and expect a ResponseStatusException
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.checkout("shivansh@gmail.com")
        );

        // Verify the exception contains a 400 Bad Request status
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Cannot checkout an empty cart", exception.getReason());
    }
}
