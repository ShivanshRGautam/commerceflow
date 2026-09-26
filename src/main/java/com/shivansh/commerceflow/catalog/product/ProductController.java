package com.shivansh.commerceflow.catalog.product;

import com.shivansh.commerceflow.catalog.product.dto.ProductRequest;
import com.shivansh.commerceflow.catalog.product.dto.ProductResponse;
import com.shivansh.commerceflow.common.response.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(
                productService.getProductById(productId)
        );
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(
                productService.getProducts(
                        page,
                        size,
                        sortBy,
                        direction,
                        categoryId
                )
        );
    }
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(
                productService.updateProduct(productId, request)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deactivateProduct(
            @PathVariable Long productId
    ) {
        productService.deactivateProduct(productId);

        return ResponseEntity.noContent().build();
    }
}