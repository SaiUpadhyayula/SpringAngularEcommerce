package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories")
    public List<CategoryDto> findAll() {
        List<Category> categories = categoryRepository.findAll();
        return categories
                .stream()
                .map(category -> new CategoryDto(category.getName()))
                .collect(toList());

    }
}
