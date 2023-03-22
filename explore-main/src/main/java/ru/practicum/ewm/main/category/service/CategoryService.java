package ru.practicum.ewm.main.category.service;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.category.model.CategoryDto;
import ru.practicum.ewm.main.category.model.CategoryMapper;
import ru.practicum.ewm.main.category.model.ShortCategoryDto;
import ru.practicum.ewm.main.category.repo.CategoryRepository;
import ru.practicum.ewm.main.exceptions.ConflictException;
import ru.practicum.ewm.main.exceptions.NotFoundException;
import ru.practicum.ewm.main.validator.Validator;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final Validator validator;

    public CategoryDto createCategory(CategoryDto category) {
        validator.validateCategory(category);
        log.info("Получен запрос на создание категории {}", category.getName());
        if (categoryRepository.findAll()
                .stream()
                .anyMatch(c -> c.getName().equals(category.getName()))) {
            throw new ConflictException("Имя уже используется");
        }
        return CategoryMapper.INSTANCE.toDto(categoryRepository.save(CategoryMapper.INSTANCE.toCategory(category)));
    }

    public void deleteCategory(Long id) {
        log.debug("Получен запрос на удаление категории {}", id);
        categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или недоступен"));
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "Условия выполнения не соблюдены");
        }
    }

    public Object update(Long id, ShortCategoryDto updatingDto) {
        validator.validateCategoryForUpd(updatingDto);
        log.debug("Получен запрос обновления категории пользователем с id {}", id);
        Category stored = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        checkNameForUniq(updatingDto);
        CategoryMapper.INSTANCE.updateCategory(updatingDto, stored);
        Category actualCategory = categoryRepository.save(stored);
        return CategoryMapper.INSTANCE.toDto(actualCategory);
    }

    public Object getCategoryById(Long id) {
        log.debug("Получен запрос на получение категории {}", id);
        Category stored = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Запрашиваемый объект не найден или не доступен"));
        return CategoryMapper.INSTANCE.toDto(stored);
    }

    public Object getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        log.info("Поиск всех категорий с пагинацией");
        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    private void checkNameForUniq(ShortCategoryDto updatingDto) {
        if (StringUtils.isNotBlank(updatingDto.getName()) && categoryRepository.findAll().stream()
                .anyMatch(u -> u.getName().equals(updatingDto.getName()))) {
            throw new ConflictException("Имя категории уже используется");
        }
    }
}
