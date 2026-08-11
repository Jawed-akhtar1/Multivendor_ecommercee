package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.request.CategoryRequest;
import com.multivendor.ecommerce.entity.Category;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.CategoryRepository;
import com.multivendor.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category create(CategoryRequest request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("A category with this name already exists");
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .build();

        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, CategoryRequest request) {
        Category category = getById(id);

        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new BadRequestException("A category cannot be its own parent");
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParent(parent);
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }

    @Override
    public List<Category> getTopLevel() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Override
    public List<Long> getDescendantIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        collectDescendantIds(categoryId, ids);
        return ids;
    }

    @Override
    public List<Category> getBreadcrumb(Long categoryId) {
        List<Category> path = new ArrayList<>();
        Category current = getById(categoryId);
        // Guard against a pathological cycle (shouldn't happen given the self-parent check
        // in update(), but avoids an infinite loop if the data ever ends up inconsistent).
        int hops = 0;
        while (current != null && hops++ < 20) {
            path.add(0, current);
            current = current.getParent();
        }
        return path;
    }

    private void collectDescendantIds(Long categoryId, List<Long> acc) {
        acc.add(categoryId);
        for (Category child : categoryRepository.findByParentId(categoryId)) {
            collectDescendantIds(child.getId(), acc);
        }
    }
}
