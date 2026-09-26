package com.shivansh.commerceflow.catalog.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategory_IdAndActiveTrue(
            Long categoryId,
            Pageable pageable
    );
}