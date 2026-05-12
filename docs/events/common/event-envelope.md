# 공통 이벤트 Envelope

모든 이벤트는 아래 구조를 따른다.

```json
{
  "meta": {
    "eventId": "evt-example-001",
    "eventType": "ExampleEvent",
    "occurredAt": "2026-02-28T09:00:00Z"
  },
  "payload": {}
}
```

## meta

| Field | Type | Required | Description |
|---|---|---|---|
| meta.eventId | string | yes | 이벤트 고유 식별자 |
| meta.eventType | string | yes | 이벤트 타입명 |
| meta.occurredAt | string(date-time) | yes | 이벤트 발생 시각. ISO-8601 UTC |

## payload

- `payload`는 이벤트별 비즈니스 데이터를 담는다.
- 상세 필드는 각 이벤트 문서의 `Payload` 섹션에서 정의한다.
