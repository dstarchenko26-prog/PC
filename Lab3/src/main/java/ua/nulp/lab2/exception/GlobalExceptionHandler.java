package ua.nulp.lab2.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========================================================================
    // 400 BAD REQUEST (Помилки валідації та некоректного JSON)
    // ========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Некоректний формат JSON запиту", request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    // ========================================================================
    // 401 UNAUTHORIZED (Помилки автентифікації - невірний логін/пароль)
    // ========================================================================

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Невірні облікові дані або відсутній токен", request.getRequestURI());
    }

    // ========================================================================
    // 403 FORBIDDEN (Немає прав доступу - наприклад, спроба змінити роль без прав ADMIN)
    // ========================================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "У вас немає прав для виконання цієї дії", request.getRequestURI());
    }

    // ========================================================================
    // 404 NOT FOUND (Ресурс не знайдено)
    // ========================================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    // ========================================================================
    // 409 CONFLICT (Конфлікт даних - наприклад, дублікат email у БД)
    // ========================================================================

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceConflictException(
            ResourceConflictException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    // Перехоплюємо виняток бази даних (коли спрацьовує UNIQUE constraint у PostgreSQL)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Порушення унікальності даних (можливо, такий запис вже існує)", request.getRequestURI());
    }

    // ========================================================================
    // 500 INTERNAL SERVER ERROR (Всі інші непередбачувані помилки)
    // ========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        // У реальному проєкті тут має бути логування помилки (наприклад, через SLF4J)
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутрішня помилка сервера: " + ex.getMessage(), request.getRequestURI());
    }

    // ========================================================================
    // Допоміжний метод для формування стандартизованої відповіді
    // ========================================================================

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, String path) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}