# stock.reservation.failed

- Topic: `stock.reservation.failed`
- Event Type: `StockReservationFailedEvent`
- Producer: `inventory-service`
- Consumers: `order-service`
- Version: `v1`

## Semantics

재고 예약 처리에 실패했음을 알리는 이벤트다.

이 이벤트를 수신한 consumer는 주문 중단과 후속 보상 처리를 시작하는 트리거로 사용한다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `StockReservationFailedEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| productId | integer | yes | 상품 ID |
| orderId | string | yes | 주문 ID |
| quantity | integer | yes | 예약 요청 수량 |
| errorCode | string | yes | 실패 코드 |

## Example

```json
{
  "meta": {
    "eventId": "evt-stock-failed-001",
    "eventType": "StockReservationFailedEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "productId": 2001,
    "orderId": "order-001",
    "quantity": 2,
    "errorCode": "INSUFFICIENT_STOCK"
  }
}
```

## Schema

- `stock.reservation.failed.schema.json`
- `../common/event-meta.schema.json`
