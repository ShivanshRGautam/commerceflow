package com.shivansh.commerceflow.cart;

import com.shivansh.commerceflow.cart.dto.AddCartItemRequest;
import com.shivansh.commerceflow.cart.dto.CartItemResponse;
import com.shivansh.commerceflow.cart.dto.CartResponse;
import com.shivansh.commerceflow.cart.dto.UpdateCartItemRequest;
import com.shivansh.commerceflow.catalog.product.Product;
import com.shivansh.commerceflow.catalog.product.ProductRepository;
import com.shivansh.commerceflow.inventory.Inventory;
import com.shivansh.commerceflow.inventory.InventoryRepository;
import com.shivansh.commerceflow.user.AppUser;
import com.shivansh.commerceflow.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public CartResponse getCart(String email) {

        AppUser user = findUser(email);
        Cart cart = findOrCreateCart(user);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(
            String email,
            AddCartItemRequest request
    ) {

        AppUser user = findUser(email);
        Cart cart = findOrCreateCart(user);
        Product product = findActiveProduct(request.productId());
        Inventory inventory = findInventory(product.getId());

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                )
                .orElse(null);

        int newQuantity = request.quantity();

        if (cartItem != null) {
            newQuantity =
                    cartItem.getQuantity() + request.quantity();
        }

        validateAvailableStock(inventory, newQuantity);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(
            String email,
            Long productId,
            UpdateCartItemRequest request
    ) {

        AppUser user = findUser(email);
        Cart cart = findExistingCart(user);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not present in your cart"
                ));

        Inventory inventory = findInventory(productId);

        validateAvailableStock(
                inventory,
                request.quantity()
        );

        cartItem.setQuantity(request.quantity());
        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(
            String email,
            Long productId
    ) {

        AppUser user = findUser(email);
        Cart cart = findExistingCart(user);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not present in your cart"
                ));

        cartItemRepository.delete(cartItem);

        return buildCartResponse(cart);
    }

    @Transactional
    public void clearCart(String email) {

        AppUser user = findUser(email);
        Cart cart = findExistingCart(user);

        cartItemRepository.deleteByCartId(cart.getId());
    }

    private AppUser findUser(String email) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));
    }

    private Cart findOrCreateCart(AppUser user) {

        return cartRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    private Cart findExistingCart(AppUser user) {

        return cartRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart was not found"
                ));
    }

    private Product findActiveProduct(Long productId) {

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + productId
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product is not currently available"
            );
        }

        return product;
    }

    private Inventory findInventory(Long productId) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Inventory is not available for product: "
                                + productId
                ));
    }

    private void validateAvailableStock(
            Inventory inventory,
            int requestedQuantity
    ) {

        if (requestedQuantity
                > inventory.getAvailableQuantity()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only "
                            + inventory.getAvailableQuantity()
                            + " units are currently available"
            );
        }
    }

    private CartResponse buildCartResponse(Cart cart) {

        List<CartItem> cartItems =
                cartItemRepository
                        .findByCartIdOrderByCreatedAtAsc(
                                cart.getId()
                        );

        List<CartItemResponse> itemResponses =
                cartItems.stream()
                        .map(this::mapItemToResponse)
                        .toList();

        int totalQuantity = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new CartResponse(
                cart.getId(),
                itemResponses,
                totalQuantity,
                totalAmount
        );
    }

    private CartItemResponse mapItemToResponse(
            CartItem cartItem
    ) {

        Product product = cartItem.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(
                        BigDecimal.valueOf(
                                cartItem.getQuantity()
                        )
                );

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getImageUrl(),
                product.getPrice(),
                cartItem.getQuantity(),
                subtotal
        );
    }
}
