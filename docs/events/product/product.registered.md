# product.registered

- Topic: `product.registered`
- Event Type: `ProductRegisteredEvent`
- Producer: `product-service`
- Consumers: `inventory-service`
- Version: `v1`

## Semantics

상품이 신규 등록되었음을 알리는 이벤트다.

이 이벤트를 수신한 consumer는 재고 엔티티를 초기화하는 트리거로 사용한다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `ProductRegisteredEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| productId | integer | yes | 상품 ID |

## Example

```json
{
  "meta": {
    "eventId": "evt-product-registered-001",
    "eventType": "ProductRegisteredEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "productId": 2001
  }
}
```

## Schema

- `product.registered.schema.json`
- `../common/event-meta.schema.json`
