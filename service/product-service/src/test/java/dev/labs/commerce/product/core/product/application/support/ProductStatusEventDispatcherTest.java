package dev.labs.commerce.product.core.product.application.support;

import dev.labs.commerce.product.core.product.application.event.ProductActivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDeactivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDiscontinuedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.InvalidProductStatusException;
import dev.labs.commerce.product.core.product.domain.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProductStatusEventDispatcherTest {

    @Mock
    private ProductEventPublisher productEventPublisher;

    @InjectMocks
    private ProductStatusEventDispatcher productStatusEventDispatcher;

    @Test
    @DisplayName("previousStatus가 null이면 IllegalArgumentException이 발생한다")
    void dispatch_whenPreviousStatusIsNull_throwsException() {
        // given
        final Product product = ProductFixture.builder().withSample().build();

        // when & then
        assertThatThrownBy(() -> productStatusEventDispatcher.dispatch(null, product))
                .isInstanceOf(IllegalArgumentException.class);
        then(productEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("product가 null이면 IllegalArgumentException이 발생한다")
    void dispatch_whenProductIsNull_throwsException() {
        // when & then
        assertThatThrownBy(() -> productStatusEventDispatcher.dispatch(ProductStatus.ACTIVE, null))
                .isInstanceOf(IllegalArgumentException.class);
        then(productEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이전 상태와 새 상태가 같으면 이벤트가 발행되지 않는다")
    void dispatch_whenStatusUnchanged_doesNothing() {
        // given
        final Product product = ProductFixture.builder()
                .withSample()
                .productStatus(ProductStatus.ACTIVE)
                .build();

        // when
        productStatusEventDispatcher.dispatch(ProductStatus.ACTIVE, product);

        // then
        then(productEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("ACTIVE로 전이하면 ProductActivatedEvent를 발행한다")
    void dispatch_toActive_publishesActivatedEvent() {
        // given
        final Long productId = 100L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.ACTIVE)
                .build();

        // when
        productStatusEventDispatcher.dispatch(ProductStatus.DRAFT, product);

        // then
        final ArgumentCaptor<ProductActivatedEvent> captor = ArgumentCaptor.forClass(ProductActivatedEvent.class);
        then(productEventPublisher).should().publishProductActivated(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("INACTIVE로 전이하면 ProductDeactivatedEvent를 발행한다")
    void dispatch_toInactive_publishesDeactivatedEvent() {
        // given
        final Long productId = 100L;
        final Product product = ProductFixture.builder()
                .withSample()
                .productId(productId)
                .productStatus(ProductStatus.INACTIVE)
                .build();

        // when
        productStatusEventDispatcher.dispatch(ProductStatus.ACTIVE, product);

        // then
        final ArgumentCaptor<ProductDeactivatedEvent> captor = ArgumentCaptor.forClass(ProductDeactivatedEvent.class);
        then(productEventPublisher).should().publishProductDeactivated(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("ACTIVE → DISCONTINUED 전이 시 ProductDiscontinuedEvent를 발행한다")
    void dispatch_activeToDiscontinued_publishesDiscontinuedEvent() {
        // given
        final Product product = ProductFixture.builder()
                .withSample()
                .productStatus(ProductStatus.DISCONTINUED)
                .build();

        // when
        productStatusEventDispatcher.dispatch(ProductStatus.ACTIVE, product);

        // then
        then(productEventPublisher).should().publishProductDiscontinued(any(ProductDiscontinuedEvent.class));
    }

    @Test
    @DisplayName("INACTIVE → DISCONTINUED 전이 시 ProductDiscontinuedEvent를 발행한다")
    void dispatch_inactiveToDiscontinued_publishesDiscontinuedEvent() {
        // given
        final Product product = ProductFixture.builder()
                .withSample()
                .productStatus(ProductStatus.DISCONTINUED)
                .build();

        // when
        productStatusEventDispatcher.dispatch(ProductStatus.INACTIVE, product);

        // then
        then(productEventPublisher).should().publishProductDiscontinued(any(ProductDiscontinuedEvent.class));
    }

    @Test
    @DisplayName("DRAFT → DISCONTINUED 전이 시 이벤트는 발행되지 않는다")
    void dispatch_draftToDiscontinued_doesNotPublishEvent() {
        // given - 한 번도 공개된 적 없으므로 폐기 이벤트 생략
        final Product product = ProductFixture.builder()
                .withSample()
                .productStatus(ProductStatus.DISCONTINUED)
                .build();

        // when
        productStatusEventDispatcher.dispatch(ProductStatus.DRAFT, product);

        // then
        then(productEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("product의 status가 null이면 InvalidProductStatusException이 발생한다")
    void dispatch_whenProductStatusIsNull_throwsException() {
        // given
        final Product product = ProductFixture.builder()
                .withSample()
                .productStatus(null)
                .build();

        // when & then
        assertThatThrownBy(() -> productStatusEventDispatcher.dispatch(ProductStatus.ACTIVE, product))
                .isInstanceOf(InvalidProductStatusException.class);
        then(productEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("DRAFT로 전이하면 도메인 규칙 위반으로 InvalidProductStatusException이 발생한다")
    void dispatch_toDraft_throwsInvalidProductStatusException() {
        // given
        final Product product = ProductFixture.builder()
                .withSample()
                .productStatus(ProductStatus.DRAFT)
                .build();

        // when & then
        assertThatThrownBy(() -> productStatusEventDispatcher.dispatch(ProductStatus.ACTIVE, product))
                .isInstanceOf(InvalidProductStatusException.class);
        then(productEventPublisher).shouldHaveNoInteractions();
    }
}
