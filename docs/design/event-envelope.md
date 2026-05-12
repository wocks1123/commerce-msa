# Kafka Event Envelope Convention

## 목적

서비스 간 Kafka 이벤트는 공통 Envelope 구조를 사용한다.

이 규약의 목적은 다음과 같다.

- 이벤트 메타데이터와 비즈니스 페이로드를 분리한다.
- 서비스별 구현 차이와 무관하게 일관된 메시지 형태를 유지한다.
- 라우팅, 추적, 검증, 멱등 처리 기준을 공통화한다.

## 표준 메시지 구조

모든 Kafka 이벤트는 다음 JSON 구조를 따른다.

```json
{
  "meta": {
    "eventId": "5b1f9e0e-1111-2222-3333-444444444444",
    "eventType": "OrderPaidEvent",
    "occurredAt": "2026-02-28T09:00:00.000Z"
  },
  "payload": {
    "orderId": "ord-001",
    "items": [
      {
        "productId": 100,
        "quantity": 2
      }
    ]
  }
}
```

## 메타 필드 규약

| 필드 | 타입 | 필수 | 규칙 |
|---|---|---|---|
| `meta.eventId` | string | yes | 이벤트마다 새로 생성되는 고유 식별자 |
| `meta.eventType` | string | yes | 이벤트 계약 이름 |
| `meta.occurredAt` | string(date-time) | yes | 이벤트 발생 시각. ISO-8601 UTC 형식 |

### 메타 필드 규칙

- `meta.eventId`는 동일 이벤트를 식별하기 위한 값이어야 한다.
- `meta.eventType`은 이벤트 종류를 식별하는 고정 이름이어야 한다.
- `meta.occurredAt`은 UTC 기준 시각이어야 한다.
- `meta`에는 공통 추적에 필요한 정보만 둔다.
- 비즈니스 데이터는 `meta`가 아니라 `payload`에 둔다.

## 페이로드 규약

- `payload`는 이벤트별 비즈니스 데이터를 담는다.
- `payload`의 상세 필드 정의는 개별 이벤트 계약 문서에서 관리한다.
- 공통 규약 문서는 이벤트별 상세 필드를 중복 정의하지 않는다.

## 발행 규칙

- 이벤트는 도메인 상태 변경이 확정된 이후 발행해야 한다.
- 메시지 키는 동일 집계 단위의 순서 보장이 필요한 식별자를 사용해야 한다.
- 일반적으로 메시지 키는 `orderId`, `paymentId`, `productId` 같은 애그리게이트 식별자를 사용한다.
- 동일한 의미의 이벤트는 동일한 Envelope 구조를 유지해야 한다.

## 소비 규칙

- consumer는 Envelope 구조를 기준으로 메시지를 해석해야 한다.
- consumer는 `meta`와 `payload`를 분리해서 처리해야 한다.
- consumer는 `meta`를 추적, 로깅, 멱등 처리의 기준으로 사용할 수 있어야 한다.
- consumer는 자신이 필요한 `payload` 계약만 해석하며, 다른 서비스의 내부 클래스 구조에 의존하지 않는다.

## 호환성 정책

- `payload` 필드 추가는 하위 호환이다.
- `payload` 필드 이름 변경 또는 삭제는 비호환 변경이다.
- enum 값의 의미 변경은 비호환 변경이다.
- `meta.eventType` 변경은 비호환 변경이다.
- 기존 consumer가 기대하는 필드의 타입 변경은 비호환 변경이다.

## 멱등성 규칙

- 모든 이벤트는 at-least-once 전달을 전제로 한다.
- consumer는 중복 수신을 안전하게 처리할 수 있어야 한다.
- 멱등 처리 기준은 `meta.eventId` 또는 비즈니스 키를 사용할 수 있다.
- 동일한 상태 전이가 이미 반영된 경우 consumer는 no-op 처리할 수 있어야 한다.

## 개별 이벤트 계약 문서와의 관계

- 이 문서는 모든 Kafka 이벤트에 공통으로 적용되는 Envelope 규약을 정의한다.
- 각 이벤트의 `payload` 구조, 의미, producer, consumer, 예시는 개별 이벤트 계약 문서에서 정의한다.
- 개별 이벤트 문서는 이 문서의 Envelope 규약을 전제로 작성한다.
