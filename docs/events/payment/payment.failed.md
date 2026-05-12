# payment.failed

- Topic: `payment.failed`
- Event Type: `PaymentFailedEvent`
- Producer: `payment-service`
- Consumers: `order-service`
- Version: `v1`

## Semantics

결제 실패가 확정되었음을 알리는 이벤트다.

이 이벤트를 수신한 consumer는 주문 중단과 후속 보상 처리를 시작하는 트리거로 사용한다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `PaymentFailedEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| paymentId | string | yes | 결제 ID |
| orderId | string | yes | 주문 ID |
| customerId | integer | yes | 고객 ID |
| failureCode | string | yes | 실패 코드 |
| failureMessage | string or null | no | 실패 상세 메시지 |
| failedAt | string(date-time) | yes | 결제 실패 시각. ISO-8601 UTC |

## Example

```json
{
  "meta": {
    "eventId": "evt-pay-failed-001",
    "eventType": "PaymentFailedEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "paymentId": "pay-001",
    "orderId": "order-001",
    "customerId": 1001,
    "failureCode": "PG_DECLINED",
    "failureMessage": "Card declined",
    "failedAt": "2026-02-28T09:00:00Z"
  }
}
```

## Schema

- `payment.failed.schema.json`
- `../common/event-meta.schema.json`
