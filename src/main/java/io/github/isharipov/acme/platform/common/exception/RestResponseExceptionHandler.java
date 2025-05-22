package io.github.isharipov.acme.platform.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.github.isharipov.acme.platform.auth.infrastructure.exception.UserAlreadyExistsException;
import io.github.isharipov.acme.platform.common.exception.model.ErrorType;
import io.github.isharipov.acme.platform.common.exception.model.FieldValidationError;
import io.github.isharipov.acme.platform.common.exception.model.GlobalValidationError;
import io.github.isharipov.acme.platform.common.exception.model.HttpServletRequestLog;
import io.github.isharipov.acme.platform.project.external.infrastructure.exception.ExternalProjectAlreadyExistsException;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    protected ResponseEntity<?> handleArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request, Locale locale) {
        logException(ex, new HttpServletRequestLog(request));
        var bindingResult = ex.getBindingResult();
        var fieldErrors = bindingResult.getFieldErrors().stream()
                .map(fieldError -> new FieldValidationError(fieldError.getField(), messageSource.getMessage(fieldError, locale)))
                .toList();
        var globalErrors = bindingResult.getGlobalErrors().stream()
                .map(globalError -> new GlobalValidationError(globalError.getObjectName(), messageSource.getMessage(globalError, locale)))
                .toList();
        return ErrorType.VALIDATION_ERROR.getErrorResponse(Stream.concat(fieldErrors.stream(), globalErrors.stream()).collect(Collectors.toList()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.VALIDATION_ERROR.getErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({EntityNotFoundException.class, UserProfileNotFoundException.class})
    public ResponseEntity<?> handleEntityExceptions(Exception ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        HttpStatus httpStatus = resolveAnnotatedResponseStatus(ex);
        return ErrorType.ENTITY_ERROR.getErrorResponse(httpStatus, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
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

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleClientAlreadyRegisteredException(UserAlreadyExistsException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.USER_ALREADY_REGISTERED_ERROR.getErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ExternalProjectAlreadyExistsException.class)
    public ResponseEntity<?> handleExternalProjectAlreadyExistsException(ExternalProjectAlreadyExistsException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.EXTERNAL_PROJECT_ALREADY_EXISTS_ERROR.getErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenMismatchException.class)
    public ResponseEntity<?> handleRefreshTokenMismatch(RefreshTokenMismatchException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.AUTH_ERROR.getErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.AUTH_ERROR.getErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<?> handleJwtAuthenticationException(JwtAuthenticationException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.AUTH_ERROR.getErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.INTERNAL_ERROR.getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        logException(ex, new HttpServletRequestLog(request));
        return ErrorType.AUTH_ERROR.getErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    private HttpStatus resolveAnnotatedResponseStatus(Exception e) {
        logger.debug("Resolving annotated response status for exception [{}]", e.getClass());
        ResponseStatus annotation = findMergedAnnotation(e.getClass(), ResponseStatus.class);
        return annotation != null ? annotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private void logException(Exception ex, HttpServletRequestLog request) {
        logger.error("Exception handler {}: {}\n{}", ex.getClass().getSimpleName(), ex.getMessage(), request);
    }
}
