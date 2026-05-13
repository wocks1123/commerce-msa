package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.support.ProductStatusEventDispatcher;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.InvalidProductStatusException;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import dev.labs.commerce.product.core.product.domain.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ChangeProductStatusUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductStatusEventDispatcher productStatusEventDispatcher;

    @InjectMocks
    private ChangeProductStatusUseCase changeProductStatusUseCase;

    @Test
    @DisplayName("상품을 찾지 못하면 ProductNotFoundException이 발생한다")
    void execute_whenProductNotFound_throwsException() {
        // given
        final Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());
        final ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, ProductStatus.ACTIVE);

        // when & then
        assertThatThrownBy(() -> changeProductStatusUseCase.execute(command))
                .isInstanceOf(ProductNotFoundException.class);
        then(productRepository).should(never()).save(any(Product.class));
        then(productStatusEventDispatcher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상품 상태를 변경하면 변경 후 상태가 결과에 반영되고 dispatcher가 호출된다")
    void execute_validTransition_dispatchesEvent() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.DRAFT)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        final ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, ProductStatus.ACTIVE);

        // when
        final ChangeProductStatusResult actual = changeProductStatusUseCase.execute(command);

        // then
        assertThat(actual.productStatus()).isEqualTo(ProductStatus.ACTIVE);
        then(productStatusEventDispatcher).should().dispatch(ProductStatus.DRAFT, product);
    }

    @Test
    @DisplayName("허용되지 않는 상태 전이는 InvalidProductStatusException이 발생한다")
    void execute_invalidTransition_throwsException() {
        // given - DRAFT → INACTIVE 는 불가
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.DRAFT)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        final ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, ProductStatus.INACTIVE);

        // when & then
        assertThatThrownBy(() -> changeProductStatusUseCase.execute(command))
                .isInstanceOf(InvalidProductStatusException.class);
        then(productRepository).should(never()).save(any(Product.class));
        then(productStatusEventDispatcher).shouldHaveNoInteractions();
    }
}
