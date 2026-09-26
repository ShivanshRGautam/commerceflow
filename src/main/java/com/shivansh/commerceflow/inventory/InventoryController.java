package com.shivansh.commerceflow.inventory;

import com.shivansh.commerceflow.inventory.dto.InventoryCreateRequest;
import com.shivansh.commerceflow.inventory.dto.InventoryResponse;
import com.shivansh.commerceflow.inventory.dto.RestockRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/products")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<InventoryResponse> createInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(
                        productId,
                        request
                ));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                inventoryService.getInventory(productId)
        );
    }

    @PatchMapping("/{productId}/restock")
    public ResponseEntity<InventoryResponse> restock(
            @PathVariable Long productId,
            @Valid @RequestBody RestockRequest request
    ) {
        return ResponseEntity.ok(
                inventoryService.restock(productId, request)
        );
    }
}
