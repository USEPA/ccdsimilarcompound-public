package gov.epa.ccte.api.similarcompounds.web.rest.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unused")
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequestMapping(produces = "application/vnd.error+json")
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({SQLException.class})
    ResponseEntity<Problem> simpleNoDataFoundException(SQLException ex){
        log.info("Exception called {} on path {}", ex.getClass().getName());

        String msg = ex.getMessage();

        Problem problem = Problem.create()
                .withTitle(HttpStatus.BAD_REQUEST.name())
                .withDetail(msg)
                .withStatus(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<Problem>(problem, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NoCompoundFoundException.class, SmilesBadFormatException.class})
    ResponseEntity<Problem> simpleNoDataFoundException(RuntimeException ex, WebRequest request){
        log.info("Exception called {} on path {}", ex.getClass().getName(), request.getContextPath());

        String msg = ex.getMessage();

        Problem problem = Problem.create()
                .withTitle(HttpStatus.BAD_REQUEST.name())
                .withDetail(msg)
                .withStatus(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<Problem>(problem, HttpStatus.BAD_REQUEST);
    }

    // for this exception to be called make sure spring.resources.add-mapping=false in application properties
    // if I do false then I won't be able to show any static contents
    @ExceptionHandler({ResourceNotFoundException.class})
    ResponseEntity<Problem> notFoundIdHandler(ResourceNotFoundException ex, WebRequest request) {
        log.info("Exception called {} on path {}", ex.getClass().getName(), request.getContextPath());

        // defined error message is confusing
        // String msg = ex.getMessage();
        String msg = "Requested resource not found.";

        Problem problem = Problem.create()
                .withTitle(HttpStatus.BAD_REQUEST.name())
                .withDetail(msg)
                .withStatus(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<Problem>(problem, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(RepositoryConstraintViolationException.class)
    protected ResponseEntity<Problem> handleConstraintViolation(RepositoryConstraintViolationException ex, WebRequest request) {
        log.info("Exception called {}, Global Errors={}, Field Errors = {} ", ex.getClass().getName(), ex.getErrors().getGlobalErrorCount(), ex.getErrors().getFieldErrorCount());

        String errorMsgs = ex.getErrors().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(";"));

        Problem problem = Problem.create()
                .withTitle(HttpStatus.NOT_FOUND.name())
                .withDetail(errorMsgs)
                .withStatus(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<Problem>(problem, HttpStatus.BAD_REQUEST);
    }

    // Following are override methods for different error situations.
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatusCode status, WebRequest webRequest) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, webRequest, ex.getClass().getSimpleName(), ex.getMessage());

    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Exception called {} ", ex.getClass().getName());

        return getStandardError(status, request, ex.getClass().getSimpleName(), ex.getMessage());

    }

    private ResponseEntity<Object> getStandardError(HttpStatusCode status, WebRequest request, String simpleName, String message) {

        Problem problem = Problem.create()
                .withTitle(status.toString())
                .withDetail(message)
                .withStatus((HttpStatus) status);

        return new ResponseEntity<Object>(problem, status);
    }
}

