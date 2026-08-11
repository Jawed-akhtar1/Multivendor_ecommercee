package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.CategoryRequest;
import com.multivendor.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService {
    Category create(CategoryRequest request);
    Category update(Long id, CategoryRequest request);
    void delete(Long id);
    List<Category> getTopLevel();
    List<Category> getAll();
    Category getById(Long id);

    /** The category's own id plus every descendant's id (recursively) — used to make
     *  browsing a parent category include products filed under its subcategories. */
    List<Long> getDescendantIds(Long categoryId);

    /** Root-to-leaf path for breadcrumbs, e.g. [Electronics, Phones, Smartphones]. */
    List<Category> getBreadcrumb(Long categoryId);
}
