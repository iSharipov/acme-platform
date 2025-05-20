package io.github.isharipov.acme.platform.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.github.isharipov.acme.platform.auth.infrastructure.UserAlreadyExistsException;
import io.github.isharipov.acme.platform.common.exception.model.ErrorType;
import io.github.isharipov.acme.platform.common.exception.model.FieldValidationError;
import io.github.isharipov.acme.platform.common.exception.model.GlobalValidationError;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@ControllerAdvice
public class RestResponseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestResponseExceptionHandler.class);

    private final MessageSource messageSource;

    public RestResponseExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleArgumentNotValidException(MethodArgumentNotValidException ex, Locale locale) {
        logger.error("Validation error", ex);
        var bindingResult = ex.getBindingResult();
        var fieldErrors = bindingResult.getFieldErrors().stream()
                .map(fieldError -> new FieldValidationError(fieldError.getField(), messageSource.getMessage(fieldError, locale)))
                .toList();
        var globalErrors = bindingResult.getGlobalErrors().stream()
                .map(globalError -> new GlobalValidationError(globalError.getObjectName(), messageSource.getMessage(globalError, locale)))
                .toList();
        return ErrorType.VALIDATION_ERROR.getErrorResponse(Stream.concat(fieldErrors.stream(), globalErrors.stream()).collect(Collectors.toList()));
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<?> handleFieldValidationException(FieldValidationException ex, Locale locale) {
        logger.error("Validation error", ex);
        return ErrorType.VALIDATION_ERROR.getErrorResponse(
                Collections.singletonList(new FieldValidationError(ex.getFieldName(),
                        messageSource.getMessage(ex.getFieldMessage(), null, locale))));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        logger.error("Validation error", e);
        return ErrorType.VALIDATION_ERROR.getErrorResponse(e.getMessage());
    }

    @ExceptionHandler({EntityNotFoundException.class, UserProfileNotFoundException.class})
    public ResponseEntity<?> handleEntityExceptions(Exception e) {
        logger.error("Entity not found", e);
        HttpStatus httpStatus = resolveAnnotatedResponseStatus(e);
        return ErrorType.ENTITY_ERROR.getErrorResponse(httpStatus, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.error("Http message not readable", ex);
        var cause = ex.getCause();
        var description = cause.getMessage();
        if (cause instanceof InvalidFormatException ife) {
            description = String.format(
                    "%s is not valid value for %s", ife.getValue(), ife.getPath().get(0).getFieldName());
        } else if (cause instanceof JsonMappingException jme) {
            description = String.format(
                    "Unable to map request JSON: %s", jme.getPath().get(0).getFieldName());
        }
        return ErrorType.HTTP_MESSAGE_NOT_READABLE.getErrorResponse(description);
    }

    @ExceptionHandler(JsonPathNotFoundException.class)
    public ResponseEntity<?> handleJsonPathNotFoundException(JsonPathNotFoundException e) {
        logger.error("Json path not found", e);
        return ErrorType.JSON_PATH_NOT_FOUND.getErrorResponse(e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleClientAlreadyRegisteredException(UserAlreadyExistsException e) {
        logger.error("User already registered: {}", e.getMessage());
        return ErrorType.USER_ALREADY_REGISTERED_ERROR.getErrorResponse(e.getMessage());
    }

    @ExceptionHandler(RefreshTokenMismatchException.class)
    public ResponseEntity<?> handleRefreshTokenMismatch(RefreshTokenMismatchException e) {
        logger.error("Refresh token mismatch: {}", e.getMessage());
        return ErrorType.AUTH_ERROR.getErrorResponse(e.getMessage());
    }

    private HttpStatus resolveAnnotatedResponseStatus(Exception e) {
        logger.debug("Resolving annotated response status for exception [{}]", e.getClass());
        ResponseStatus annotation = findMergedAnnotation(e.getClass(), ResponseStatus.class);
        return annotation != null ? annotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
