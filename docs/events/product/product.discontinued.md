# product.discontinued

- Topic: `product.discontinued`
- Event Type: `ProductDiscontinuedEvent`
- Producer: `product-service`
- Consumers: `-`
- Version: `v1`

## Semantics

상품이 판매 종료 상태로 전이되었음을 알리는 이벤트다.

현재 프로젝트 기준 등록된 consumer는 없지만, 검색 인덱스 제거 또는 외부 노출 차단 같은 후속 확장 지점으로 사용할 수 있다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `ProductDiscontinuedEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| productId | integer | yes | 상품 ID |

## Example

```json
{
  "meta": {
    "eventId": "evt-product-discontinued-001",
    "eventType": "ProductDiscontinuedEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "productId": 2001
  }
}
```

## Schema

- `product.discontinued.schema.json`
- `../common/event-meta.schema.json`
