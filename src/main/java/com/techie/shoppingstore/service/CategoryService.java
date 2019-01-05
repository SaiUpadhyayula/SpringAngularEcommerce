package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.CategoryDto;
import com.techie.shoppingstore.model.Category;
import com.techie.shoppingstore.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> findAll() {
        List<Category> categories = categoryRepository.findAll();

        return categories
                .stream()
                .map(category -> new CategoryDto(category.getName()))
                .collect(Collectors.toList());

    }
}
