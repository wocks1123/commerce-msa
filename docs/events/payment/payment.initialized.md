# payment.initialized

- Topic: `payment.initialized`
- Event Type: `PaymentInitializedEvent`
- Producer: `payment-service`
- Consumers: `order-service`
- Version: `v1`

## Semantics

결제 초기화가 완료되었음을 알리는 이벤트다.

이 이벤트를 수신한 consumer는 주문을 결제 대기 상태로 전이하는 트리거로 사용한다.

## Envelope

공통 envelope 규칙은 [event-envelope.md](/D:/dev/project/labs/commerce-msa/docs/events/common/event-envelope.md)를 따른다.

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventType | string | yes | `PaymentInitializedEvent` |
| payload | object | yes | 비즈니스 데이터 |

## Payload

| Field | Type | Required | Description |
|---|---|---|---|
| paymentId | string | yes | 결제 ID |
| orderId | string | yes | 주문 ID |
| requestedAt | string(date-time) | yes | 결제 초기화 시각. ISO-8601 UTC |

## Example

```json
{
  "meta": {
    "eventId": "evt-pay-init-001",
    "eventType": "PaymentInitializedEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {
    "paymentId": "pay-001",
    "orderId": "order-001",
    "requestedAt": "2026-02-28T09:00:00Z"
  }
}
```

## Schema

- `payment.initialized.schema.json`
- `../common/event-meta.schema.json`
