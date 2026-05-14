package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.query.ProductQueryService;
import dev.labs.commerce.product.core.product.application.usecase.dto.GetProductResult;
import dev.labs.commerce.product.core.product.domain.ProductCategory;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import dev.labs.commerce.product.core.product.domain.error.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetProductUseCaseTest {

    @Mock
    private ProductQueryService productQueryService;

    @InjectMocks
    private GetProductUseCase getProductUseCase;

    @Test
    @DisplayName("상품 조회 시 ProductQueryService의 결과를 그대로 반환한다")
    void execute_returnsQueryServiceResult() {
        // given
        final Long productId = 1L;
        final GetProductResult result = sampleResult(productId);
        given(productQueryService.getProduct(productId)).willReturn(result);

        // when
        final GetProductResult actual = getProductUseCase.execute(productId);

        // then
        assertThat(actual).isEqualTo(result);
    }

    @Test
    @DisplayName("ProductQueryService에서 예외가 발생하면 그대로 전파한다")
    void execute_whenQueryServiceThrows_propagatesException() {
        // given
        final Long productId = 999L;
        given(productQueryService.getProduct(productId))
                .willThrow(new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> getProductUseCase.execute(productId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    private GetProductResult sampleResult(Long productId) {
        final Instant now = Instant.now();
        return new GetProductResult(
                productId,
                "상품명",
                10000L, 9000L, "KRW",
                ProductStatus.DRAFT,
                ProductCategory.FIGURE,
                null, null, null, "설명",
                now, now
        );
    }
}
