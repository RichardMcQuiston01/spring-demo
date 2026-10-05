package com.mcq.taskapi.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maps every failure to the {@link ErrorResponse} shape. Spring MVC's own client errors (bad
 * parameters, malformed bodies, unknown routes, wrong methods) are inherited from
 * {@link ResponseEntityExceptionHandler} so they keep their 4xx status instead of becoming a 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Object> handleTaskNotFound(TaskNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, new HttpHeaders(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception ex, WebRequest request) {
        log.error("Unexpected error handling request {}", requestPath(request), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while processing the request",
                request,
                new HttpHeaders(),
                null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponse.FieldError(
                        fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return buildResponse(status, "Validation failed for one or more fields", request, headers, fieldErrors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return buildResponse(status, describeUnreadableBody(ex), request, headers, null);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String parameterName = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : ex.getPropertyName();
        String message = "Parameter '%s' must be %s, but received '%s'"
                .formatted(parameterName, describeExpectedType(ex.getRequiredType()), ex.getValue());

        return buildResponse(status, message, request, headers, null);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = "No endpoint found for %s %s"
                .formatted(((ServletWebRequest) request).getHttpMethod(), requestPath(request));

        return buildResponse(status, message, request, headers, null);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        String message = ex instanceof org.springframework.web.ErrorResponse springError
                && springError.getBody().getDetail() != null
                ? springError.getBody().getDetail()
                : HttpStatus.valueOf(statusCode.value()).getReasonPhrase();

        return buildResponse(statusCode, message, request, headers, null);
    }

    private ResponseEntity<Object> buildResponse(
            HttpStatusCode statusCode,
            String message,
            WebRequest request,
            HttpHeaders headers,
            @Nullable List<ErrorResponse.FieldError> fieldErrors) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(), status.getReasonPhrase(), message, requestPath(request), fieldErrors);

        return ResponseEntity.status(statusCode).headers(headers).body(errorResponse);
    }

    private String requestPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }

    private String describeUnreadableBody(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {
            String fieldName = invalidFormat.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            return "Invalid value '%s' for field '%s': must be %s"
                    .formatted(invalidFormat.getValue(), fieldName, describeExpectedType(invalidFormat.getTargetType()));
        }
        return "Request body is missing or is not valid JSON";
    }

    private String describeExpectedType(@Nullable Class<?> expectedType) {
        if (expectedType == null) {
            return "a valid value";
        }
        if (expectedType.isEnum()) {
            return "one of " + Arrays.toString(expectedType.getEnumConstants());
        }
        return "a valid " + expectedType.getSimpleName();
    }
}
