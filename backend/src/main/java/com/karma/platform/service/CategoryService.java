package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.CatalogDtos;
import com.karma.platform.persistence.entity.CategoryEntity;
import com.karma.platform.persistence.repository.CategoryRepository;
import com.karma.platform.persistence.repository.ThemeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ThemeRepository themeRepository;
    private final ApiMapper apiMapper;

    public CategoryService(CategoryRepository categoryRepository, ThemeRepository themeRepository, ApiMapper apiMapper) {
        this.categoryRepository = categoryRepository;
        this.themeRepository = themeRepository;
        this.apiMapper = apiMapper;
    }

    public List<CatalogDtos.CategoryResponse> categories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparingInt(CategoryEntity::getSortOrder))
                .map(apiMapper::toCategory)
                .toList();
    }

    public CatalogDtos.CategoryDetailsResponse details(String slug) {
        CategoryEntity category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.category-not-found", "Category not found"));
        return new CatalogDtos.CategoryDetailsResponse(
                apiMapper.toCategory(category),
                themeRepository.findByCategoryIdOrderBySortOrderAsc(category.getId()).stream().map(apiMapper::toTheme).toList()
        );
    }
}
