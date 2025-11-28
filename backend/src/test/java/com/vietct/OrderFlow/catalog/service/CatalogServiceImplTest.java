package com.vietct.OrderFlow.catalog.service;

import com.vietct.OrderFlow.catalog.domain.Category;
import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.exception.CategoryNotFoundException;
import com.vietct.OrderFlow.catalog.exception.ProductNotFoundException;
import com.vietct.OrderFlow.catalog.repository.CategoryRepository;
import com.vietct.OrderFlow.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Product sampleProduct(Category category) {
        return new Product(
                "Wireless Noise-Cancelling Headphones",
                "Test description",
                new BigDecimal("199.90"),
                10,
                null,
                category
        );
    }

    private Category sampleCategory() {
        return new Category(
                "Electronics",
                "electronics",
                "Devices, gadgets, and accessories"
        );
    }

    @Test
    void getProductById_existingProduct_returnsProduct() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Category category = sampleCategory();
        Product product = sampleProduct(category);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        Product result = catalogService.getProductById(productId);

        // Assert
        assertThat(result).isSameAs(product);
        verify(productRepository).findById(productId);
        verifyNoMoreInteractions(productRepository, categoryRepository);
    }

    @Test
    void getProductById_missingProduct_throwsProductNotFoundException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> catalogService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(productId.toString());

        verify(productRepository).findById(productId);
        verifyNoMoreInteractions(productRepository, categoryRepository);
    }

    @Test
    void getProducts_noCategoryId_callsFindAllWithPageable() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());
        Category category = sampleCategory();
        Product product1 = sampleProduct(category);
        Product product2 = sampleProduct(category);

        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(page);

        // Act
        var resultPage = catalogService.getProducts(null, pageable);

        // Assert
        assertThat(resultPage.getContent()).containsExactly(product1, product2);
        assertThat(resultPage.getTotalElements()).isEqualTo(2);

        verify(productRepository).findAll(pageable);
        verifyNoMoreInteractions(productRepository, categoryRepository);
    }

    @Test
    void getProducts_withExistingCategory_callsFindByCategoryId() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());
        Category category = sampleCategory();
        Product product = sampleProduct(category);

        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(page);

        // Act
        var resultPage = catalogService.getProducts(categoryId, pageable);

        // Assert
        assertThat(resultPage.getContent()).containsExactly(product);
        assertThat(resultPage.getTotalElements()).isEqualTo(1);

        verify(categoryRepository).existsById(categoryId);
        verify(productRepository).findByCategoryId(categoryId, pageable);
        verifyNoMoreInteractions(productRepository, categoryRepository);
    }

    @Test
    void getProducts_withMissingCategory_throwsCategoryNotFoundException() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 2);

        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> catalogService.getProducts(categoryId, pageable))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining(categoryId.toString());

        verify(categoryRepository).existsById(categoryId);
        verifyNoInteractions(productRepository);
    }
}
