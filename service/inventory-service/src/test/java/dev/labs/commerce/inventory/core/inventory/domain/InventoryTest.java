package dev.labs.commerce.inventory.core.inventory.domain;

import dev.labs.commerce.inventory.core.inventory.domain.error.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

    @Nested
    @DisplayName("재고 등록")
    class Create {

        @Test
        @DisplayName("상품 ID로 재고를 등록하면 초기 수량은 0이다")
        void createInventory_withProductId_startsWithZeroQuantity() {
            // given
            Long productId = 1L;

            // when
            Inventory inventory = Inventory.create(productId);

            // then
            assertThat(inventory.getProductId()).isEqualTo(productId);
            assertThat(inventory.getTotalQuantity()).isZero();
            assertThat(inventory.getReservedQuantity()).isZero();
        }

        @Test
        @DisplayName("상품 ID가 없으면 재고를 등록할 수 없다")
        void createInventory_withNullProductId_throwsException() {
            // when & then
            assertThatThrownBy(() -> Inventory.create(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("가용 재고 조회")
    class GetAvailableQuantity {

        @Test
        @DisplayName("가용 재고는 전체 수량에서 예약 수량을 뺀 값이다")
        void getAvailableQuantity_returnsTotal_minusReserved() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(3);

            // when
            int available = inventory.getAvailableQuantity();

            // then
            assertThat(available).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("재고 입고")
    class Increase {

        @Test
        @DisplayName("입고 수량만큼 전체 재고가 증가한다")
        void increase_addsQuantityToTotal() {
            // given
            Inventory inventory = Inventory.create(1L);

            // when
            inventory.increase(10);

            // then
            assertThat(inventory.getTotalQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("0 이하의 수량은 입고할 수 없다")
        void increase_withZeroOrNegativeQty_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);

            // when & then
            assertThatThrownBy(() -> inventory.increase(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("재고 출고")
    class Decrease {

        @Test
        @DisplayName("출고 수량만큼 전체 재고가 감소한다")
        void decrease_subtractsQuantityFromTotal() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);

            // when
            inventory.decrease(4);

            // then
            assertThat(inventory.getTotalQuantity()).isEqualTo(6);
        }

        @Test
        @DisplayName("0 이하의 수량은 출고할 수 없다")
        void decrease_withZeroOrNegativeQty_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);

            // when & then
            assertThatThrownBy(() -> inventory.decrease(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("출고 후 남은 수량이 예약 수량보다 적어지면 출고할 수 없다")
        void decrease_belowReservedQuantity_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(8);

            // when & then
            assertThatThrownBy(() -> inventory.decrease(5))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("재고 예약")
    class Reserve {

        @Test
        @DisplayName("가용 재고 이내의 수량을 예약하면 예약 수량이 증가한다")
        void reserve_withinAvailableStock_increasesReservedQuantity() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);

            // when
            inventory.reserve(3);

            // then
            assertThat(inventory.getReservedQuantity()).isEqualTo(3);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("0 이하의 수량은 예약할 수 없다")
        void reserve_withZeroOrNegativeQty_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);

            // when & then
            assertThatThrownBy(() -> inventory.reserve(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("가용 재고를 초과한 수량은 예약할 수 없다")
        void reserve_exceedingAvailableStock_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(5);

            // when & then
            assertThatThrownBy(() -> inventory.reserve(6))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("재고 예약 해제")
    class Release {

        @Test
        @DisplayName("예약 수량만큼 해제하면 예약 수량이 감소한다")
        void release_decreasesReservedQuantity() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(5);

            // when
            inventory.release(3);

            // then
            assertThat(inventory.getReservedQuantity()).isEqualTo(2);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(8);
        }

        @Test
        @DisplayName("0 이하의 수량은 해제할 수 없다")
        void release_withZeroOrNegativeQty_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(5);

            // when & then
            assertThatThrownBy(() -> inventory.release(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("예약 수량을 초과하여 해제할 수 없다")
        void release_exceedingReservedQuantity_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(3);

            // when & then
            assertThatThrownBy(() -> inventory.release(4))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("재고 예약 확정")
    class Confirm {

        @Test
        @DisplayName("예약 확정하면 예약 수량과 전체 수량이 함께 감소한다")
        void confirm_decreasesReservedAndTotalQuantity() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(5);

            // when
            inventory.confirm(5);

            // then
            assertThat(inventory.getReservedQuantity()).isZero();
            assertThat(inventory.getTotalQuantity()).isEqualTo(5);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("예약 수량 중 일부만 확정할 수 있다")
        void confirm_partialReservation_decreasesCorrectly() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(5);

            // when
            inventory.confirm(3);

            // then
            assertThat(inventory.getReservedQuantity()).isEqualTo(2);
            assertThat(inventory.getTotalQuantity()).isEqualTo(7);
            assertThat(inventory.getAvailableQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("0 이하의 수량은 확정할 수 없다")
        void confirm_withZeroOrNegativeQty_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(5);

            // when & then
            assertThatThrownBy(() -> inventory.confirm(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("예약 수량을 초과하여 확정할 수 없다")
        void confirm_exceedingReservedQuantity_throwsException() {
            // given
            Inventory inventory = Inventory.create(1L);
            inventory.increase(10);
            inventory.reserve(3);

            // when & then
            assertThatThrownBy(() -> inventory.confirm(4))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

}
