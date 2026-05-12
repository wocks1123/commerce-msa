# order.paid

- Topic: `order.paid`
- Event Type: `OrderPaidEvent`
- Producer: `order-service`
- Consumers: `inventory-service`
- Version: `v1`

## Semantics

주문이 결제 완료 상태로 확정되었음을 알리는 이벤트다.

이 이벤트를 수신한 consumer는 예약 재고를 확정하는 트리거로 사용한다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `OrderPaidEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| orderId | string | yes | 주문 ID |
| items | array | yes | 주문 상품 목록. 비어 있을 수 없다 |
| items[].productId | integer | yes | 상품 ID |
| items[].quantity | integer | yes | 주문 수량 |

## Example

```json
{
  "meta": {
    "eventId": "evt-order-paid-001",
    "eventType": "OrderPaidEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "orderId": "order-001",
    "items": [
      {
        "productId": 2001,
        "quantity": 2
      },
      {
        "productId": 2002,
        "quantity": 1
      }
    ]
  }
}
```

## Schema

- `order.paid.schema.json`
- `../common/event-meta.schema.json`
