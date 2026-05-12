# payment.approved

- Topic: `payment.approved`
- Event Type: `PaymentApprovedEvent`
- Producer: `payment-service`
- Consumers: `order-service`
- Version: `v1`

## Semantics

결제 승인 완료를 알리는 이벤트다.

이 이벤트를 수신한 consumer는 결제가 최종 승인되었음을 기준으로 후속 상태 전이를 수행한다.
동일 이벤트가 중복 전달될 수 있으므로 consumer는 `meta.eventId` 또는 비즈니스 키 기준 멱등성을 보장해야 한다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `PaymentApprovedEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| paymentId | string | yes | 결제 ID |
| orderId | string | yes | 주문 ID |
| customerId | integer | yes | 고객 ID |
| amount | integer | yes | 승인 금액 |
| currency | string | yes | 통화 코드. 예: `KRW` |
| approvedAt | string(date-time) | yes | 결제 승인 시각. ISO-8601 UTC |

## Example

```json
{
  "meta": {
    "eventId": "evt-pay-approved-001",
    "eventType": "PaymentApprovedEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "paymentId": "pay-001",
    "orderId": "order-001",
    "customerId": 1001,
    "amount": 30000,
    "currency": "KRW",
    "approvedAt": "2026-02-28T09:00:00Z"
  }
}
```

## Schema

- `payment.approved.schema.json`
- `../common/event-meta.schema.json`
