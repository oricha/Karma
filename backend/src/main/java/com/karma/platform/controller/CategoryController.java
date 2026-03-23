package com.karma.platform.controller;

import com.karma.platform.dto.CatalogDtos;
import com.karma.platform.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CatalogDtos.CategoryResponse> categories() {
        return categoryService.categories();
    }

    @GetMapping("/{slug}")
    public CatalogDtos.CategoryDetailsResponse details(@PathVariable String slug) {
        return categoryService.details(slug);
    }

    @GetMapping("/{slug}/themes")
    public List<CatalogDtos.ThemeResponse> themes(@PathVariable String slug) {
        return categoryService.details(slug).themes();
    }
}
