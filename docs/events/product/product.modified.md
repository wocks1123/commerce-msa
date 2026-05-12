# product.modified

- Topic: `product.modified`
- Event Type: `ProductModifiedEvent`
- Producer: `product-service`
- Consumers: `-`
- Version: `v1`

## Semantics

활성 또는 비활성 상태 상품의 공개 정보가 변경되었음을 알리는 이벤트다.

현재 프로젝트 기준 등록된 consumer는 없지만, 카탈로그 동기화나 검색 인덱스 갱신 같은 후속 확장 지점으로 사용할 수 있다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `ProductModifiedEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| productId | integer | yes | 상품 ID |
| productName | string | yes | 상품명 |
| listPrice | integer | yes | 정가 |
| sellingPrice | integer | yes | 판매가 |
| currency | string | yes | 통화 코드 |
| category | string | yes | 상품 카테고리 |
| saleStartAt | string(date-time) or null | no | 판매 시작 시각 |
| saleEndAt | string(date-time) or null | no | 판매 종료 시각 |
| thumbnailUrl | string or null | no | 썸네일 URL |
| description | string | yes | 상품 설명 |

## Example

```json
{
  "meta": {
    "eventId": "evt-product-modified-001",
    "eventType": "ProductModifiedEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "productId": 2001,
    "productName": "RX-78-2 Figure",
    "listPrice": 35000,
    "sellingPrice": 32000,
    "currency": "KRW",
    "category": "FIGURE",
    "saleStartAt": "2026-02-28T09:00:00Z",
    "saleEndAt": null,
    "thumbnailUrl": "https://example.com/products/2001.png",
    "description": "판매 정보가 수정된 피규어 상품"
  }
}
```

## Schema

- `product.modified.schema.json`
- `../common/event-meta.schema.json`
