package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductModifiedEvent;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductCategory;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import dev.labs.commerce.product.core.product.domain.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ModifyProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventPublisher productEventPublisher;

    @InjectMocks
    private ModifyProductUseCase modifyProductUseCase;

    @Test
    @DisplayName("상품을 수정하면 변경된 정보를 반환한다")
    void execute_modifiesProductAndReturnsResult() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder().withSample().productId(productId).build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);

        final ModifyProductCommand command = new ModifyProductCommand(
                productId, "수정된 상품명", 20000L, 18000L, "KRW",
                ProductCategory.PLUSH, null, null, null, "수정된 설명"
        );

        // when
        final ModifyProductResult actual = modifyProductUseCase.execute(command);

        // then
        assertThat(actual.productName()).isEqualTo("수정된 상품명");
        assertThat(actual.listPrice()).isEqualTo(20000L);
        assertThat(actual.sellingPrice()).isEqualTo(18000L);
        assertThat(actual.category()).isEqualTo(ProductCategory.PLUSH);
    }

    @Test
    @DisplayName("상품을 찾지 못하면 ProductNotFoundException이 발생한다")
    void execute_whenProductNotFound_throwsException() {
        // given
        final Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());
        final ModifyProductCommand command = new ModifyProductCommand(
                productId, "이름", 10000L, 9000L, "KRW",
                ProductCategory.FIGURE, null, null, null, "설명"
        );

        // when & then
        assertThatThrownBy(() -> modifyProductUseCase.execute(command))
                .isInstanceOf(ProductNotFoundException.class);
        then(productRepository).should(never()).save(any(Product.class));
        then(productEventPublisher).should(never()).publishProductModified(any());
    }

    @Test
    @DisplayName("ACTIVE 상품의 정보를 변경하면 ProductModifiedEvent가 발행된다")
    void execute_whenActiveAndChanged_publishesEvent() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.ACTIVE)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);

        final ModifyProductCommand command = new ModifyProductCommand(
                productId, "변경된 이름", 10000L, 9000L, "KRW",
                ProductCategory.FIGURE, null, null, null, "설명"
        );

        // when
        modifyProductUseCase.execute(command);

        // then
        final ArgumentCaptor<ProductModifiedEvent> captor = ArgumentCaptor.forClass(ProductModifiedEvent.class);
        then(productEventPublisher).should().publishProductModified(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
        assertThat(captor.getValue().productName()).isEqualTo("변경된 이름");
    }

    @Test
    @DisplayName("INACTIVE 상품의 정보를 변경하면 ProductModifiedEvent가 발행된다")
    void execute_whenInactiveAndChanged_publishesEvent() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.INACTIVE)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);

        final ModifyProductCommand command = new ModifyProductCommand(
                productId, "변경된 이름", 10000L, 9000L, "KRW",
                ProductCategory.FIGURE, null, null, null, "설명"
        );

        // when
        modifyProductUseCase.execute(command);

        // then
        then(productEventPublisher).should().publishProductModified(any(ProductModifiedEvent.class));
    }

    @Test
    @DisplayName("DRAFT 상품의 정보를 변경해도 이벤트는 발행되지 않는다")
    void execute_whenDraft_doesNotPublishEvent() {
        // given - DRAFT 상태는 공개되지 않음
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.DRAFT)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);

        final ModifyProductCommand command = new ModifyProductCommand(
                productId, "변경된 이름", 10000L, 9000L, "KRW",
                ProductCategory.FIGURE, null, null, null, "설명"
        );

        // when
        modifyProductUseCase.execute(command);

        // then
        then(productEventPublisher).should(never()).publishProductModified(any());
    }

    @Test
    @DisplayName("ACTIVE 상품이지만 실제 변경 필드가 없으면 이벤트는 발행되지 않는다")
    void execute_whenNoFieldChanged_doesNotPublishEvent() {
        // given
        final Long productId = 1L;
        final Instant now = Instant.now();
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.ACTIVE)
                .productName("같은이름")
                .listPrice(10000L)
                .sellingPrice(9000L)
                .currency("KRW")
                .category(ProductCategory.FIGURE)
                .description("같은설명")
                .createdAt(now)
                .updatedAt(now)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);

        final ModifyProductCommand command = new ModifyProductCommand(
                productId, "같은이름", 10000L, 9000L, "KRW",
                ProductCategory.FIGURE, null, null, null, "같은설명"
        );

        // when
        modifyProductUseCase.execute(command);

        // then
        then(productEventPublisher).should(never()).publishProductModified(any());
    }
}
