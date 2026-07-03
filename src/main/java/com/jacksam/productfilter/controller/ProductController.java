package com.jacksam.productfilter.controller;

import com.jacksam.productfilter.dto.BatchCreateRequest;
import com.jacksam.productfilter.dto.CreateProductRequest;
import com.jacksam.productfilter.dto.ProductDTO;
import com.jacksam.productfilter.dto.ProductFilterRequest;
import com.jacksam.productfilter.entity.Category;
import com.jacksam.productfilter.repository.CategoryRepository;
import com.jacksam.productfilter.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getProducts(
            Authentication auth,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean includeSubCategories,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = (Long) auth.getPrincipal();
        ProductFilterRequest filter = new ProductFilterRequest(
                search, minPrice, maxPrice, categoryId,
                includeSubCategories, inStock, active, sortBy, order, page, size);

        return ResponseEntity.ok(productService.getAccessibleProducts(userId, filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(productService.getProduct(userId, id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            Authentication auth,
            @Valid @RequestBody CreateProductRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(productService.createProduct(userId, req));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductDTO>> batchCreate(
            Authentication auth,
            @Valid @RequestBody BatchCreateRequest req) {
        Long userId = (Long) auth.getPrincipal();
        List<ProductDTO> results = req.products().stream()
                .map(item -> productService.createProduct(userId, new CreateProductRequest(
                        item.name(), item.description(), item.price(),
                        item.quantity(), item.categoryId(), item.imageUrl())))
                .toList();
        return ResponseEntity.ok(results);
    }
}
