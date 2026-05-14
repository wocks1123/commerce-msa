package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.query.ProductQueryService;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsResult;
import dev.labs.commerce.product.core.product.domain.ProductCategory;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ListProductsUseCaseTest {

    @Mock
    private ProductQueryService productQueryService;

    @InjectMocks
    private ListProductsUseCase listProductsUseCase;

    @Test
    @DisplayName("status가 주어지면 해당 상태로 필터링된 결과를 그대로 반환한다")
    void execute_withStatus_returnsFilteredResult() {
        // given
        final ProductStatus status = ProductStatus.ACTIVE;
        final List<ListProductsResult> result = List.of(sampleResult(1L), sampleResult(2L));
        given(productQueryService.listProducts(status)).willReturn(result);

        // when
        final List<ListProductsResult> actual = listProductsUseCase.execute(status);

        // then
        assertThat(actual).isEqualTo(result);
        then(productQueryService).should().listProducts(status);
    }

    @Test
    @DisplayName("status가 null이면 전체 결과를 그대로 반환한다")
    void execute_withNullStatus_returnsAllResults() {
        // given
        final List<ListProductsResult> result = List.of(sampleResult(1L));
        given(productQueryService.listProducts(null)).willReturn(result);

        // when
        final List<ListProductsResult> actual = listProductsUseCase.execute(null);

        // then
        assertThat(actual).isEqualTo(result);
    }

    @Test
    @DisplayName("결과가 없으면 빈 리스트를 반환한다")
    void execute_whenNoResults_returnsEmptyList() {
        // given
        given(productQueryService.listProducts(ProductStatus.DRAFT)).willReturn(List.of());

        // when
        final List<ListProductsResult> actual = listProductsUseCase.execute(ProductStatus.DRAFT);

        // then
        assertThat(actual).isEmpty();
    }

    private ListProductsResult sampleResult(Long productId) {
        return new ListProductsResult(
                productId,
                "상품" + productId,
                10000L, 9000L, "KRW",
                ProductStatus.ACTIVE,
                ProductCategory.FIGURE,
                null
        );
    }
}
