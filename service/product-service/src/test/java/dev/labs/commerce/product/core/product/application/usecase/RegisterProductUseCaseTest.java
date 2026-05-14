package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductRegisteredEvent;
import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductResult;
import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductCategory;
import dev.labs.commerce.product.core.product.domain.ProductRepository;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RegisterProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventPublisher productEventPublisher;

    @InjectMocks
    private RegisterProductUseCase registerProductUseCase;

    @Test
    @DisplayName("상품을 등록하면 저장된 상품 정보를 반환한다")
    void execute_savesProductAndReturnsResult() {
        // given
        final RegisterProductCommand command = sampleCommand();
        final Product savedProduct = ProductFixture.builder()
                .withSample()
                .productId(100L)
                .productName(command.productName())
                .listPrice(command.listPrice())
                .sellingPrice(command.sellingPrice())
                .currency(command.currency())
                .category(command.category())
                .description(command.description())
                .build();
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        final RegisterProductResult actual = registerProductUseCase.execute(command);

        // then
        assertThat(actual.productId()).isEqualTo(100L);
        assertThat(actual.productName()).isEqualTo(command.productName());
        assertThat(actual.productStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    @DisplayName("상품 저장 후 ProductRegisteredEvent를 발행한다")
    void execute_publishesProductRegisteredEvent() {
        // given
        final RegisterProductCommand command = sampleCommand();
        final Product savedProduct = ProductFixture.builder()
                .withSample()
                .productId(100L)
                .build();
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        registerProductUseCase.execute(command);

        // then
        final ArgumentCaptor<ProductRegisteredEvent> captor = ArgumentCaptor.forClass(ProductRegisteredEvent.class);
        then(productEventPublisher).should().publishProductRegistered(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Repository의 save가 호출된다")
    void execute_invokesRepositorySave() {
        // given
        final RegisterProductCommand command = sampleCommand();
        final Product savedProduct = ProductFixture.builder().withSample().productId(100L).build();
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        registerProductUseCase.execute(command);

        // then
        then(productRepository).should().save(any(Product.class));
    }

    @Test
    @DisplayName("도메인 검증에 실패하면 저장과 이벤트 발행이 일어나지 않는다")
    void execute_whenDomainValidationFails_doesNotSaveOrPublish() {
        // given - 판매가 > 정가 (도메인 검증 실패)
        final RegisterProductCommand invalidCommand = new RegisterProductCommand(
                "상품명", 10000L, 11000L, "KRW",
                ProductCategory.FIGURE, null, null, null, "설명"
        );

        // when & then
        assertThatThrownBy(() -> registerProductUseCase.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class);
        then(productRepository).should(never()).save(any(Product.class));
        then(productEventPublisher).should(never()).publishProductRegistered(any());
    }

    private RegisterProductCommand sampleCommand() {
        final Instant saleStart = Instant.now().plus(1, ChronoUnit.DAYS);
        final Instant saleEnd = Instant.now().plus(10, ChronoUnit.DAYS);
        return new RegisterProductCommand(
                "테스트 상품", 10000L, 9000L, "KRW",
                ProductCategory.FIGURE, saleStart, saleEnd, "https://cdn.example.com/thumb.jpg",
                "상품 설명"
        );
    }
}
