package com.example.storesports.infrastructure.exceptions;

import com.example.storesports.infrastructure.utils.ResponseData;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalException {

    @ExceptionHandler({ResponseStatusException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData<String> handleValidationException(Exception e) {
        log.error("Validation error: {}", e.getMessage());
        return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Dữ liệu không hợp lệ: " + e.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseData<Map<String, String>>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("name", e.getMessage()); // key có thể thay theo field tương ứng
        return ResponseEntity.badRequest().body(
                new ResponseData<>(400, "Dữ liệu không hợp lệ", errors)
        );
    }

    // Xử lý lỗi hệ thống chung
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseData<String> handleException(Exception e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống, vui lòng thử lại sau");
    }

    @ExceptionHandler(NameNotExists.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData<Map<String, String>> handleNameNotExists(NameNotExists e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("name", e.getMessage());
        return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Tên đã tồn tại", errors);
    }


    @ExceptionHandler(AttributeValueDuplicate.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseData<String> handleErrorAttributeValueDuplicate(AttributeValueDuplicate ex) {
        log.warn("Conflict error: {}", ex.getMessage());
        return new ResponseData<>(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    // Xử lý lỗi hệ thống chung
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ResponseData<String> handleException(Exception e) {
//        log.error("Internal server error: {}", e.getMessage(), e);
//        return new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống: " + e.getMessage());
//    }

    // Xử lý lỗi quyền truy cập
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseData<String> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseData<>(HttpStatus.FORBIDDEN.value(), "Lỗi quyền truy cập: " + ex.getMessage());
    }

    // Xử lý lỗi người dùng không tồn tại
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseData<String> handleUsernameNotFound(UsernameNotFoundException ex) {
        return new ResponseData<>(HttpStatus.NOT_FOUND.value(), "Người dùng không tồn tại: " + ex.getMessage());
    }
    @ExceptionHandler(CartEmptyException.class)
    public ResponseData<Void> handleCartEmptyException(CartEmptyException e) {
        return new ResponseData<>(400, e.getMessage());
    }

    // Xử lý lỗi ResponseStatusException
//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseData<String> handleResponseStatusException(ResponseStatusException ex) {
//        return new ResponseData<>(ex.getStatusCode().value(), ex.getReason());
//    }

    // Xử lý lỗi validate tham số
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Dữ liệu không hợp lệ", errors);
    }

    // Xử lý lỗi ràng buộc cơ sở dữ liệu
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseData<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Dữ liệu không hợp lệ", errors);
    }

    // Xử lý lỗi vi phạm ràng buộc SQL
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseData<String> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL constraint violation: {}", ex.getMessage());
        return new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi cơ sở dữ liệu: " + ex.getMessage());
    }

    // checktrung
    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<Map<String,String>> handleDuplicateEntityException(DuplicateEntityException ex) {
      Map<String, String> errors = new HashMap<>();
      errors.put("error", ex.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
