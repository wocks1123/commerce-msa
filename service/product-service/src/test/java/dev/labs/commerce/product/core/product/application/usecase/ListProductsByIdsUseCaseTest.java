package dev.labs.commerce.product.core.product.application.usecase;

import dev.labs.commerce.product.core.product.application.query.ProductQueryService;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsByIdsResult;
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
class ListProductsByIdsUseCaseTest {

    @Mock
    private ProductQueryService productQueryService;

    @InjectMocks
    private ListProductsByIdsUseCase listProductsByIdsUseCase;

    @Test
    @DisplayName("productIds로 조회 시 QueryService의 결과를 그대로 반환한다")
    void execute_returnsQueryServiceResult() {
        // given
        final List<Long> productIds = List.of(1L, 2L);
        final List<ListProductsByIdsResult> result = List.of(sampleResult(1L), sampleResult(2L));
        given(productQueryService.listProductsByIds(productIds)).willReturn(result);

        // when
        final List<ListProductsByIdsResult> actual = listProductsByIdsUseCase.execute(productIds);

        // then
        assertThat(actual).isEqualTo(result);
        then(productQueryService).should().listProductsByIds(productIds);
    }

    @Test
    @DisplayName("매칭되는 상품이 없으면 빈 리스트를 반환한다")
    void execute_whenNoMatch_returnsEmptyList() {
        // given
        final List<Long> productIds = List.of(999L);
        given(productQueryService.listProductsByIds(productIds)).willReturn(List.of());

        // when
        final List<ListProductsByIdsResult> actual = listProductsByIdsUseCase.execute(productIds);

        // then
        assertThat(actual).isEmpty();
    }

    private ListProductsByIdsResult sampleResult(Long productId) {
        return new ListProductsByIdsResult(
                productId,
                "상품" + productId,
                10000L, 9000L, "KRW",
                ProductStatus.ACTIVE,
                ProductCategory.FIGURE,
                null
        );
    }
}
