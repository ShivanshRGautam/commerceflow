package com.shivansh.commerceflow.order;

import com.shivansh.commerceflow.cart.Cart;
import com.shivansh.commerceflow.cart.CartItem;
import com.shivansh.commerceflow.cart.CartItemRepository;
import com.shivansh.commerceflow.cart.CartRepository;
import com.shivansh.commerceflow.catalog.product.Product;
import com.shivansh.commerceflow.inventory.Inventory;
import com.shivansh.commerceflow.inventory.InventoryRepository;
import com.shivansh.commerceflow.order.dto.OrderItemResponse;
import com.shivansh.commerceflow.order.dto.OrderResponse;
import com.shivansh.commerceflow.user.AppUser;
import com.shivansh.commerceflow.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            UserRepository userRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            InventoryRepository inventoryRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderResponse checkout(String email) {
        AppUser user = findUser(email);

        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cart was not found"
                ));

        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout an empty cart");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, Inventory> inventoryByProduct = new HashMap<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (!product.isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is no longer available: " + product.getName());
            }

            Inventory inventory = inventoryRepository
                    .findByProductId(product.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Inventory is unavailable for product: " + product.getName()));

            if (inventory.getAvailableQuantity() < cartItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Only " + inventory.getAvailableQuantity() + " units are available for " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            inventoryByProduct.put(product.getId(), inventory);
        }

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);

        CustomerOrder savedOrder = orderRepository.save(order);
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setProductSku(product.getSku());
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);

            Inventory inventory = inventoryByProduct.get(product.getId());
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - cartItem.getQuantity());
            inventoryRepository.save(inventory);
        }

        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteByCartId(cart.getId());

        return mapToResponse(savedOrder, savedItems);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        AppUser user = findUser(email);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(order -> mapToResponse(order, orderItemRepository.findByOrderId(order.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String email, Long orderId) {
        AppUser user = findUser(email);
        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with ID: " + orderId));
        return mapToResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    // --- NEW ADMIN METHODS ---

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(order -> mapToResponse(order, orderItemRepository.findByOrderId(order.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with ID: " + orderId));
        return mapToResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            return mapToResponse(order, orderItemRepository.findByOrderId(order.getId()));
        }

        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change status of a " + currentStatus + " order");
        }

        // Validate allowed transitions
        if (newStatus == OrderStatus.CANCELLED) {
            restoreInventoryForOrder(order);
        } else if (newStatus == OrderStatus.CONFIRMED && currentStatus != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only confirm a PENDING order");
        } else if (newStatus == OrderStatus.SHIPPED && currentStatus != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only ship a CONFIRMED order");
        } else if (newStatus == OrderStatus.DELIVERED && currentStatus != OrderStatus.SHIPPED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only deliver a SHIPPED order");
        }

        order.setStatus(newStatus);
        CustomerOrder savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder, orderItemRepository.findByOrderId(savedOrder.getId()));
    }

    private void restoreInventoryForOrder(CustomerOrder order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        for (OrderItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Inventory not found for product"));

            // Restore the stock
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    // --- HELPER METHODS ---

    private AppUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    private String generateOrderNumber() {
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "ORD-" + randomPart;
    }

    private OrderResponse mapToResponse(CustomerOrder order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream().map(this::mapItemToResponse).toList();
        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getStatus(), order.getTotalAmount(), itemResponses, order.getCreatedAt(), order.getUpdatedAt());
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return new OrderItemResponse(item.getId(), item.getProduct().getId(), item.getProductSku(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
    }
}