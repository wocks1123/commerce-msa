package dev.labs.commerce.common.web.doc;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ProblemDetail;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Inherited
@Target({METHOD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "504",
        description = "Gateway Timeout (External dependency timeout)",
        content = {
                @Content(schema = @Schema(implementation = ProblemDetail.class))
        }
)
public @interface ApiGatewayTimeoutResponse {
}
