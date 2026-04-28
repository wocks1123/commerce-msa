# 주요 컴포넌트

## 주문 만료 스케줄러

결제 초기화 과정에서 장애가 발생하면 주문이 `PENDING` 상태로 방치될 수 있다. 이를 처리하기 위해 order-service에 만료 스케줄러를 구현했다.

- 1분 주기로 실행되며, `CREATED` 상태인 주문 중 생성 시각 기준 10분을 초과한 것을 만료 대상으로 조회
- 대상 주문을 `EXPIRED`로 상태 전이하고 `order.expired` 이벤트 발행
- inventory-service는 이벤트를 수신해 해당 주문의 재고 예약을 해제하며, 예약이 없는 경우 멱등하게 skip 처리

만료 기준 시간은 `order.expiry.pending-expiry-minutes`(기본값 10분)로 조정 가능하다.

## 결제 만료 스케줄러

사용자가 PG 결제창에서 이탈하거나 시간 초과가 발생하면 결제 레코드가 `REQUESTED` 상태로 방치될 수 있다. 이를 처리하기 위해 payment-service에 만료 스케줄러를 구현했다.

- 1분 주기로 실행되며, `REQUESTED` 상태인 결제 중 요청 시각 기준 30분을 초과한 것을 만료 대상으로 조회
- 대상 결제를 `FAILED`(`failureCode=PAYMENT_EXPIRED`)로 상태 전이하고 `payment.expired` 이벤트 발행
- order-service는 이벤트를 수신해 해당 주문을 `EXPIRED`로 전이하고 `order.expired` 이벤트 발행
- inventory-service는 `order.expired`를 수신해 재고 예약을 해제

만료 기준 시간은 `payment.expiry.requested-expiry-minutes`(기본값 30분)로 조정 가능하다.

## Mock PG 게이트웨이

실제 PG사 연동 없이 개발/테스트 가능하도록 `PgGateway` 인터페이스와 `MockPgGateway` 구현체를 제공.
`PgGatewayRouter`가 결제 요청의 `PgProvider` 값 기반으로 구현체를 라우팅하므로, 실제 PG 구현체 추가 시 기존 코드 변경 없음.

결제 초기화 시 `idempotencyKey` 중복 여부를 사전 검사해, 네트워크 재시도로 동일 요청이 재전송되어도 이중 결제 미발생.
