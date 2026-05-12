# 이벤트 계약 문서

이 디렉토리는 서비스 간 이벤트 계약 문서를 관리한다.

각 이벤트는 사람이 읽는 Markdown 문서와 기계가 읽는 JSON Schema를 함께 가진다.

## 디렉토리 구조

```text
docs/events/
  event-contract-authoring-guide.md
  {domain}/
    {topic}.md
    {topic}.schema.json
```

## 공통 규칙

- 모든 이벤트는 `meta`와 `payload`를 가진다.
- `meta.eventId`는 이벤트 고유 식별자다.
- `meta.eventType`은 이벤트 타입을 식별하는 고정 계약 이름이다.
- `meta.occurredAt`은 ISO-8601 UTC 문자열이다.
- 이벤트 문서는 `.md`, 검증 규칙은 `.schema.json`에 둔다.
- 문서 예시 시간은 `2026-02-28T09:00:00Z`를 기본값으로 사용한다.

## 호환성 정책

- payload 필드 추가는 하위 호환이다.
- 필드 이름 변경 또는 삭제는 호환성을 깨는 변경이다.
- enum 값의 의미 변경은 호환성을 깨는 변경이다.
- `meta.eventType` 변경은 호환성을 깨는 변경이다.

## 작성 기준

- Markdown 문서는 이벤트 의미, producer, consumer, 필드 설명, 예시를 담는다.
- 공통 envelope 설명은 `common/event-envelope.md`에 두고, 이벤트 문서에는 이벤트별 차이만 적는다.
- JSON Schema는 구조 검증 규칙과 최소한의 메타데이터를 담는다.
- `topic`, `producer`, `consumers`, `version`, `semantics` 같은 운영 메타데이터는 Markdown 문서에만 둔다.
- 공통 envelope의 `meta`는 별도 schema로 분리하고 각 이벤트 schema에서 `$ref`로 재사용할 수 있다.
- Markdown의 Example과 schema의 `examples`는 같은 샘플을 사용하는 편이 좋다.
