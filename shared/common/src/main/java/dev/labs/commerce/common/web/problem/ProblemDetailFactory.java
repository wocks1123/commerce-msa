package dev.labs.commerce.common.web.problem;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ProblemDetailFactory {

    private static final URI DEFAULT_TYPE = URI.create("about:blank");

    /**
     * 일반 에러용 ProblemDetail 생성
     *
     * @param status       HTTP 상태코드 (Advice에서 결정)
     * @param detail       사용자/클라이언트용 메시지
     * @param instancePath 요청 path (예: "/products/999")
     * @param code         ErrorCode.getCode()
     */
    public ProblemDetail create(HttpStatusCode status,
                                String detail,
                                String instancePath,
                                String code) {
        return create(status, detail, instancePath, code, null);
    }

    /**
     * validation errors 포함 ProblemDetail 생성
     */
    public ProblemDetail create(HttpStatusCode status,
                                String detail,
                                String instancePath,
                                String code,
                                List<Map<String, String>> errors) {

        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(detail, "detail must not be null");
        Objects.requireNonNull(instancePath, "instancePath must not be null");
        Objects.requireNonNull(code, "code must not be null");

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);

        pd.setType(DEFAULT_TYPE);
        pd.setTitle(toTitle(status));
        pd.setInstance(URI.create(instancePath));

        // extensions
        pd.setProperty("code", code);
        if (errors != null && !errors.isEmpty()) {
            pd.setProperty("errors", errors);
        }

        return pd;
    }

    private String toTitle(HttpStatusCode status) {
        if (status instanceof HttpStatus hs) {
            return hs.getReasonPhrase(); // e.g. "Not Found"
        }
        return "Error";
    }
}
