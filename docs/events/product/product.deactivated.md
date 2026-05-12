# product.deactivated

- Topic: `product.deactivated`
- Event Type: `ProductDeactivatedEvent`
- Producer: `product-service`
- Consumers: `-`
- Version: `v1`

## Semantics

상품이 판매 중단 상태로 전이되었음을 알리는 이벤트다.

현재 프로젝트 기준 등록된 consumer는 없지만, 카탈로그 비노출이나 캐시 무효화 같은 후속 확장 지점으로 사용할 수 있다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `ProductDeactivatedEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| productId | integer | yes | 상품 ID |

## Example

```json
{
  "meta": {
    "eventId": "evt-product-deactivated-001",
    "eventType": "ProductDeactivatedEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "productId": 2001
  }
}
```

## Schema

- `product.deactivated.schema.json`
- `../common/event-meta.schema.json`
