package com.karma.platform.service;

import com.karma.platform.common.ApiException;
import com.karma.platform.dto.CatalogDtos;
import com.karma.platform.model.Category;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final PlatformDataStore dataStore;
    private final ApiMapper apiMapper;

    public CategoryService(PlatformDataStore dataStore, ApiMapper apiMapper) {
        this.dataStore = dataStore;
        this.apiMapper = apiMapper;
    }

    public List<CatalogDtos.CategoryResponse> categories() {
        return dataStore.categories().stream().map(apiMapper::toCategory).toList();
    }

    public CatalogDtos.CategoryDetailsResponse details(String slug) {
        Category category = dataStore.categoryBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        return new CatalogDtos.CategoryDetailsResponse(
                apiMapper.toCategory(category),
                dataStore.themesByCategory(category.id()).stream().map(apiMapper::toTheme).toList()
        );
    }
}
