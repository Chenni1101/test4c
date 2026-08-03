package edu.suibe.evidence.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      fields.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "请求参数校验失败", request, fields);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception, HttpServletRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    exception
        .getConstraintViolations()
        .forEach(violation -> fields.put(violation.getPropertyPath().toString(), violation.getMessage()));
    return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "请求参数校验失败", request, fields);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<ApiErrorResponse> handleNotFound(
      EntityNotFoundException exception, HttpServletRequest request) {
    return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request, Map.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiErrorResponse> handleConflict(
      DataIntegrityViolationException exception, HttpServletRequest request) {
    return response(HttpStatus.CONFLICT, "DATA_CONFLICT", "资源状态冲突或已存在", request, Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiErrorResponse> handleForbidden(
      AccessDeniedException exception, HttpServletRequest request) {
    return response(HttpStatus.FORBIDDEN, "FORBIDDEN", "没有执行此操作的权限", request, Map.of());
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ApiErrorResponse> handleBadCredentials(
      BadCredentialsException exception, HttpServletRequest request) {
    return response(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "用户名或密码错误", request, Map.of());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiErrorResponse> handleIllegalArgument(
      IllegalArgumentException exception, HttpServletRequest request) {
    return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request, Map.of());
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<ApiErrorResponse> handleState(
      IllegalStateException exception, HttpServletRequest request) {
    return response(HttpStatus.CONFLICT, "STATE_CONFLICT", exception.getMessage(), request, Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
    return response(
        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "服务内部错误", request, Map.of());
  }

  private ResponseEntity<ApiErrorResponse> response(
      HttpStatus status,
      String code,
      String message,
      HttpServletRequest request,
      Map<String, String> fieldErrors) {
    String traceId = request.getHeader("X-Request-Id");
    if (traceId == null || traceId.isBlank()) {
      traceId = request.getAttribute("traceId") instanceof String value ? value : "";
    }
    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(Instant.now(), status.value(), code, message, traceId, fieldErrors));
  }
}
