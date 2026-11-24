package payment.controller.advice;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import payment.controller.PaymentController;

@RestControllerAdvice(assignableTypes = PaymentController.class)
public class PaymentControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {


        ex.getBody().setDetail("Validation failed, " + getMessage(ex));

        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {

        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @Override
    protected ProblemDetail createProblemDetail(Exception ex,
                                                HttpStatusCode status,
                                                String defaultDetail,
                                                @Nullable String detailMessageCode,
                                                Object @Nullable [] detailMessageArguments,
                                                WebRequest request) {

        defaultDetail = switch (ex) {
            case HttpMessageNotReadableException ee -> StringUtils.substringBefore(ex.getMessage(), ":"); //"JSON parse error" expected to be left
            default -> "Internal Error";
        };

        return super.createProblemDetail(ex, status, defaultDetail, detailMessageCode, detailMessageArguments, request);
    }

    private String getMessage(MethodArgumentNotValidException ex) {
        return StringUtils.substringBetween(ex.getMessage(), " on ", ";");
    }
}
