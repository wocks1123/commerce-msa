package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.event.ProductActivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDeactivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDiscontinuedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusCommand;
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
    private ProductEventPublisher productEventPublisher;

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
    }

    @Test
    @DisplayName("DRAFT → ACTIVE 전이 시 ProductActivatedEvent를 발행한다")
    void execute_draftToActive_publishesActivatedEvent() {
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
        changeProductStatusUseCase.execute(command);

        // then
        then(productEventPublisher).should().publishProductActivated(any(ProductActivatedEvent.class));
        then(productEventPublisher).should(never()).publishProductDeactivated(any());
        then(productEventPublisher).should(never()).publishProductDiscontinued(any());
    }

    @Test
    @DisplayName("ACTIVE → INACTIVE 전이 시 ProductDeactivatedEvent를 발행한다")
    void execute_activeToInactive_publishesDeactivatedEvent() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.ACTIVE)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        final ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, ProductStatus.INACTIVE);

        // when
        changeProductStatusUseCase.execute(command);

        // then
        then(productEventPublisher).should().publishProductDeactivated(any(ProductDeactivatedEvent.class));
        then(productEventPublisher).should(never()).publishProductActivated(any());
    }

    @Test
    @DisplayName("ACTIVE → DISCONTINUED 전이 시 ProductDiscontinuedEvent를 발행한다")
    void execute_activeToDiscontinued_publishesDiscontinuedEvent() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.ACTIVE)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        final ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, ProductStatus.DISCONTINUED);

        // when
        changeProductStatusUseCase.execute(command);

        // then
        then(productEventPublisher).should().publishProductDiscontinued(any(ProductDiscontinuedEvent.class));
    }

    @Test
    @DisplayName("DRAFT → DISCONTINUED 전이 시 외부 이벤트는 발행되지 않는다")
    void execute_draftToDiscontinued_doesNotPublishEvent() {
        // given - 한 번도 공개된 적 없으므로 폐기 이벤트 생략
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.DRAFT)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        final ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, ProductStatus.DISCONTINUED);

        // when
        changeProductStatusUseCase.execute(command);

        // then
        then(productEventPublisher).should(never()).publishProductDiscontinued(any());
        then(productEventPublisher).should(never()).publishProductActivated(any());
        then(productEventPublisher).should(never()).publishProductDeactivated(any());
    }

    @Test
    @DisplayName("동일 상태로 변경하면 이벤트는 발행되지 않는다")
    void execute_whenStatusUnchanged_doesNotPublishEvent() {
        // given
        final Long productId = 1L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.DISCONTINUED)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(productRepository.save(product)).willReturn(product);
        final ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, ProductStatus.DISCONTINUED);

        // when
        changeProductStatusUseCase.execute(command);

        // then
        then(productEventPublisher).should(never()).publishProductActivated(any());
        then(productEventPublisher).should(never()).publishProductDeactivated(any());
        then(productEventPublisher).should(never()).publishProductDiscontinued(any());
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
        then(productEventPublisher).shouldHaveNoInteractions();
    }
}
