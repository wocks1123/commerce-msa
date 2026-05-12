# payment.expired

- Topic: `payment.expired`
- Event Type: `PaymentExpiredEvent`
- Producer: `payment-service`
- Consumers: `order-service`
- Version: `v1`

## Semantics

결제 유효 시간이 만료되었음을 알리는 이벤트다.

이 이벤트를 수신한 consumer는 주문 만료와 후속 보상 처리를 시작하는 트리거로 사용한다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `PaymentExpiredEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| paymentId | string | yes | 결제 ID |
| orderId | string | yes | 주문 ID |
| customerId | integer | yes | 고객 ID |
| expiredAt | string(date-time) | yes | 결제 만료 시각. ISO-8601 UTC |

## Example

```json
{
  "meta": {
    "eventId": "evt-pay-expired-001",
    "eventType": "PaymentExpiredEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "paymentId": "pay-001",
    "orderId": "order-001",
    "customerId": 1001,
    "expiredAt": "2026-02-28T09:00:00Z"
  }
}
```

## Schema

- `payment.expired.schema.json`
- `../common/event-meta.schema.json`
