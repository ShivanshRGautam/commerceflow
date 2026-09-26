package com.shivansh.commerceflow.inventory;

import com.shivansh.commerceflow.catalog.product.Product;
import com.shivansh.commerceflow.catalog.product.ProductRepository;
import com.shivansh.commerceflow.inventory.dto.InventoryCreateRequest;
import com.shivansh.commerceflow.inventory.dto.InventoryResponse;
import com.shivansh.commerceflow.inventory.dto.RestockRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponse createInventory(
            Long productId,
            InventoryCreateRequest request
    ) {

        Product product = productRepository
                .findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active product not found with ID: " + productId
                ));

        if (inventoryRepository.existsByProductId(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventory already exists for Product ID: " + productId
            );
        }

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(request.initialQuantity());

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        return convertToResponse(savedInventory);
    }

    public InventoryResponse getInventory(Long productId) {

        Inventory inventory = findByProductId(productId);

        return convertToResponse(inventory);
    }

    @Transactional
    public InventoryResponse restock(
            Long productId,
            RestockRequest request
    ) {

        Inventory inventory = findByProductId(productId);

        int newQuantity =
                inventory.getAvailableQuantity() + request.quantity();

        inventory.setAvailableQuantity(newQuantity);

        Inventory updatedInventory =
                inventoryRepository.saveAndFlush(inventory);

        return convertToResponse(updatedInventory);
    }

    private Inventory findByProductId(Long productId) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found for Product ID: " + productId
                ));
    }

    private InventoryResponse convertToResponse(
            Inventory inventory
    ) {

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getProduct().getName(),
                inventory.getAvailableQuantity(),
                inventory.getVersion(),
                inventory.getUpdatedAt()
        );
    }
}
