# API Gateway

모든 외부 요청의 단일 진입점. Spring Cloud Gateway(WebFlux/Netty) 기반이며 포트는 **20100**이다.

각 API의 요청/응답 스키마는 서비스별 `service/*/docs/openapi.json`을 참고한다.

## 라우트

| 외부 경로 | 대상 서비스 | 기본 주소 |
|---|---|---|
| `/api/v1/products/**` | product-service | `http://localhost:20101` |
| `/api/v1/inventories/**` | inventory-service | `http://localhost:20102` |
| `/api/v1/orders/**` | order-service | `http://localhost:20103` |
| `/api/v1/payments/**` | payment-service | `http://localhost:20104` |

경로 prefix가 서비스별로 겹치지 않으므로 경로 재작성 필터를 쓰지 않는다. 받은 경로를 그대로 전달한다.

`/api/v1/payments/mock-pay/success|fail`은 PG 콜백이다. PG에 등록하는 콜백 URL은
게이트웨이 주소(`:20100`)로 지정해야 한다.

서비스 간 호출은 게이트웨이를 거치지 않고 직접 통신한다.

> **라우트 순서 주의.** 등록된 Route를 위에서부터 훑어 처음 매칭된 하나로 확정한다.
> Spring MVC와 달리 구체적인 패턴이 우선하지 않는다.
> `/api/v1/**` 같은 넓은 패턴을 위에 추가하면 아래 라우트가 전부 죽는다.

> **Method predicate 주의.** 라우트를 `Method` predicate로 좁혀 화이트리스트를 만들지 않는다.
> preflight(OPTIONS)가 라우트에 매칭되지 않아 CORS가 깨진다.

## Swagger UI

통합 UI: **`http://localhost:20100/swagger-ui.html`** (드롭다운으로 네 서비스 전환)

| 게이트웨이 경로 | 대상 |
|---|---|
| `/api-docs/product` | product-service `/api-docs` |
| `/api-docs/inventory` | inventory-service `/api-docs` |
| `/api-docs/order` | order-service `/api-docs` |
| `/api-docs/payment` | payment-service `/api-docs` |

네 서비스가 모두 같은 `/api-docs` 경로를 쓰므로 `SetPath` 필터로 되돌린다. 여기만 경로 재작성을 쓴다.

> `springdoc.api-docs.enabled: false`로 두지 말 것. springdoc 자동설정 전체의 스위치라서
> swagger-ui까지 함께 꺼진다.

## 게이트웨이가 만드는 에러

다운스트림이 반환한 응답은 상태코드와 본문 모두 그대로 전달된다.
게이트웨이가 자체 에러를 만드는 것은 요청을 다운스트림에 전달하지 못했을 때뿐이다.

아래 코드들은 서비스별 OpenAPI 스펙에 없으므로 클라이언트가 별도로 처리해야 한다.

| 상황 | HTTP | `code` |
|---|---|---|
| 매칭되는 라우트 없음 | 404 | `ROUTE_NOT_FOUND` |
| 다운스트림 연결 거부 | 503 | `SERVICE_UNAVAILABLE` |
| 다운스트림 응답 타임아웃 | 504 | `GATEWAY_TIMEOUT` |
| 그 외 게이트웨이 거부 | 해당 상태코드 | `GATEWAY_ERROR` |
| 분류되지 않은 예외 | 500 | `GATEWAY_INTERNAL_ERROR` |

형식은 다른 서비스와 동일한 `application/problem+json`이다.

```json
{
  "type": "about:blank",
  "title": "Service Unavailable",
  "status": 503,
  "detail": "Downstream service is unavailable.",
  "instance": "/api/v1/orders/ord-7781",
  "code": "SERVICE_UNAVAILABLE"
}
```

## 설정

| 환경변수 | 기본값 |
|---|---|
| `SERVICE_ROUTE_PRODUCT_URI` | `http://localhost:20101` |
| `SERVICE_ROUTE_INVENTORY_URI` | `http://localhost:20102` |
| `SERVICE_ROUTE_ORDER_URI` | `http://localhost:20103` |
| `SERVICE_ROUTE_PAYMENT_URI` | `http://localhost:20104` |
| `GATEWAY_CORS_ALLOWEDORIGINPATTERNS` | `*` (쉼표 구분) |

> **운영 전환 시 필수.** CORS 기본값 `*`는 로컬 개발용이며 `allow-credentials: true`와
> 함께 쓰이므로 운영에서는 실제 도메인으로 교체해야 한다.

타임아웃은 `httpclient.connect-timeout` 3s, `httpclient.response-timeout` 10s.
각 서비스의 `RestClient` 타임아웃(connect 3s / read 5s)보다 넉넉히 두어 backstop 역할만 한다.

## 제약

- `shared:common`을 의존하지 않는다. JPA가 런타임 클래스패스에 올라오면 DataSource 자동설정이
  실패하고, `@RestControllerAdvice`는 WebFlux에 없는 서블릿 API를 쓴다.
- 블로킹 호출 금지. JPA, `RestTemplate`, `Thread.sleep`, 동기 파일 I/O 모두 해당한다.
  이벤트 루프 스레드가 막히면 전체 처리량이 떨어진다.
