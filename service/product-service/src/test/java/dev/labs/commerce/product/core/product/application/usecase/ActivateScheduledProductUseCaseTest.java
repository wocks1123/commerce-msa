package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.support.ProductStatusEventDispatcher;
import dev.labs.commerce.product.core.product.application.usecase.dto.ActivateScheduledProductCommand;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
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
class ActivateScheduledProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductStatusEventDispatcher productStatusEventDispatcher;

    @InjectMocks
    private ActivateScheduledProductUseCase activateScheduledProductUseCase;

    @Test
    @DisplayName("INACTIVE 상품을 ACTIVE로 전환하고 dispatcher가 호출된다")
    void execute_inactiveToActive_dispatchesEvent() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.INACTIVE)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        final ActivateScheduledProductCommand command = new ActivateScheduledProductCommand(productId);

        // when
        activateScheduledProductUseCase.execute(command);

        // then
        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
        then(productStatusEventDispatcher).should().dispatch(ProductStatus.INACTIVE, product);
    }

    @Test
    @DisplayName("이미 ACTIVE 상태이면 아무것도 하지 않는다")
    void execute_whenAlreadyActive_doesNothing() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.ACTIVE)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        final ActivateScheduledProductCommand command = new ActivateScheduledProductCommand(productId);

        // when
        activateScheduledProductUseCase.execute(command);

        // then
        then(productRepository).should(never()).save(any(Product.class));
        then(productStatusEventDispatcher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상품을 찾지 못하면 ProductNotFoundException이 발생한다")
    void execute_whenProductNotFound_throwsException() {
        // given
        final Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());
        final ActivateScheduledProductCommand command = new ActivateScheduledProductCommand(productId);

        // when & then
        assertThatThrownBy(() -> activateScheduledProductUseCase.execute(command))
                .isInstanceOf(ProductNotFoundException.class);
        then(productRepository).should(never()).save(any(Product.class));
        then(productStatusEventDispatcher).shouldHaveNoInteractions();
    }
}
