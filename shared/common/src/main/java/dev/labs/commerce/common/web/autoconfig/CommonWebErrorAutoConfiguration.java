package dev.labs.commerce.common.web.autoconfig;

import dev.labs.commerce.common.web.advice.CoreExceptionAdvice;
import dev.labs.commerce.common.web.advice.FallbackExceptionAdvice;
import dev.labs.commerce.common.web.advice.ValidationExceptionAdvice;
import dev.labs.commerce.common.web.problem.ProblemDetailFactory;
import dev.labs.commerce.common.web.problem.ValidationErrorMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestControllerAdvice")
public class CommonWebErrorAutoConfiguration {

    @Bean
    public ProblemDetailFactory problemDetailFactory() {
        return new ProblemDetailFactory();
    }

    @Bean
    public ValidationErrorMapper validationErrorMapper() {
        return new ValidationErrorMapper();
    }

    @Bean
    public CoreExceptionAdvice coreExceptionAdvice(ProblemDetailFactory factory) {
        return new CoreExceptionAdvice(factory);
    }

    @Bean
    public ValidationExceptionAdvice validationExceptionAdvice(
            ProblemDetailFactory factory,
            ValidationErrorMapper mapper
    ) {
        return new ValidationExceptionAdvice(factory, mapper);
    }

    @Bean
    public FallbackExceptionAdvice fallbackExceptionAdvice(ProblemDetailFactory factory) {
        return new FallbackExceptionAdvice(factory);
    }
}
