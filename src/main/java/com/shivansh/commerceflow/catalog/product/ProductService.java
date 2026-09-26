package com.shivansh.commerceflow.catalog.product;

import com.shivansh.commerceflow.catalog.category.Category;
import com.shivansh.commerceflow.catalog.category.CategoryRepository;
import com.shivansh.commerceflow.catalog.product.dto.ProductRequest;
import com.shivansh.commerceflow.catalog.product.dto.ProductResponse;
import com.shivansh.commerceflow.common.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("name", "sku", "price", "createdAt");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Create Product
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        String normalizedSku =
                request.sku().trim().toUpperCase(Locale.ROOT);

        if (productRepository.existsBySku(normalizedSku)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product already exists with SKU: " + normalizedSku
            );
        }

        Category category =
                findActiveCategory(request.categoryId());

        Product product = new Product();

        product.setSku(normalizedSku);
        product.setName(request.name().trim());
        product.setDescription(trimToNull(request.description()));
        product.setPrice(request.price());
        product.setImageUrl(trimToNull(request.imageUrl()));
        product.setCategory(category);

        Product savedProduct =
                productRepository.save(product);

        return convertToResponse(savedProduct);
    }

    // Get one active Product
    public ProductResponse getProductById(Long productId) {

        Product product = findActiveProduct(productId);

        return convertToResponse(product);
    }

    // Get paginated and sorted Products
    public PagedResponse<ProductResponse> getProducts(
            int page,
            int size,
            String sortBy,
            String direction,
            Long categoryId
    ) {

        validatePagination(
                page,
                size,
                sortBy,
                direction,
                categoryId
        );

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Product> productPage;

        if (categoryId == null) {
            productPage =
                    productRepository.findByActiveTrue(pageable);
        } else {
            productPage =
                    productRepository
                            .findByCategory_IdAndActiveTrue(
                                    categoryId,
                                    pageable
                            );
        }

        List<ProductResponse> content = productPage
                .getContent()
                .stream()
                .map(this::convertToResponse)
                .toList();

        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast()
        );
    }

    // Update an existing Product
    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            ProductRequest request
    ) {

        Product product = findActiveProduct(productId);

        String normalizedSku =
                request.sku().trim().toUpperCase(Locale.ROOT);

        if (productRepository.existsBySkuAndIdNot(
                normalizedSku,
                productId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another product already uses SKU: "
                            + normalizedSku
            );
        }

        Category category =
                findActiveCategory(request.categoryId());

        product.setSku(normalizedSku);
        product.setName(request.name().trim());
        product.setDescription(trimToNull(request.description()));
        product.setPrice(request.price());
        product.setImageUrl(trimToNull(request.imageUrl()));
        product.setCategory(category);

        Product updatedProduct =
                productRepository.saveAndFlush(product);

        return convertToResponse(updatedProduct);
    }

    // Soft-delete a Product
    @Transactional
    public void deactivateProduct(Long productId) {

        Product product = findActiveProduct(productId);

        product.setActive(false);

        productRepository.save(product);
    }

    // Find an active Product or return 404
    private Product findActiveProduct(Long productId) {

        return productRepository
                .findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active product not found with ID: "
                                + productId
                ));
    }

    // Find an active Category or return 404
    private Category findActiveCategory(Long categoryId) {

        return categoryRepository
                .findById(categoryId)
                .filter(Category::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active category not found with ID: "
                                + categoryId
                ));
    }

    // Validate pagination and sorting inputs
    private void validatePagination(
            int page,
            int size,
            String sortBy,
            String direction,
            Long categoryId
    ) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field: " + sortBy
            );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direction must be asc or desc"
            );
        }

        if (categoryId != null && categoryId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category ID must be positive"
            );
        }
    }

    // Convert internal Entity into API Response DTO
    private ProductResponse convertToResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.isActive(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    // Convert null or whitespace-only optional values to null
    private String trimToNull(String value) {

        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty()
                ? null
                : trimmedValue;
    }
}