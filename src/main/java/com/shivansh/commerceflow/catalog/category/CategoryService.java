package com.shivansh.commerceflow.catalog.category;

import com.shivansh.commerceflow.catalog.category.dto.CategoryRequest;
import com.shivansh.commerceflow.catalog.category.dto.CategoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        String categoryName = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(categoryName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category already exists: " + categoryName
            );
        }

        Category category = new Category();
        category.setName(categoryName);

        if (request.description() != null) {
            category.setDescription(request.description().trim());
        }

        Category savedCategory = categoryRepository.save(category);

        return convertToResponse(savedCategory);
    }

    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findAllByActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private CategoryResponse convertToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
