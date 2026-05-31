package com.example.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import cz.jirutka.rsql.parser.RSQLParserException;
import org.springframework.security.access.AccessDeniedException;

import vn.saolasoft.base.api.response.APIResponse;
import vn.saolasoft.base.api.response.APIResponseHeader;
import vn.saolasoft.base.api.response.APIResponseStatus;
import vn.saolasoft.base.exception.APIAuthenticationException;
import vn.saolasoft.base.exception.APIException;
import vn.saolasoft.base.exception.DuplicateIdentifierException;
import vn.saolasoft.base.exception.ObjectNotFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionController {
    // Xử lí tập trung Exception tại đây, service chỉ cần throw qua đây
    
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleNotFound(ObjectNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, APIResponseStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateIdentifierException.class)
    public ResponseEntity<APIResponse<Void>> handleDuplicate(DuplicateIdentifierException ex) {
        return build(HttpStatus.BAD_REQUEST, APIResponseStatus.DUPLICATED, ex.getMessage());
    }

    @ExceptionHandler(APIAuthenticationException.class)
    public ResponseEntity<APIResponse<Void>> handleAuth(APIAuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, APIResponseStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Catch-all cho APIException còn lại
    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse<Void>> handleAPI(APIException ex) {
        return build(HttpStatus.BAD_REQUEST, APIResponseStatus.INVALID_PARAMETER, ex.getMessage());
    }

    // @Valid fail trên @RequestBody (password blank, price âm)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, APIResponseStatus.INVALID_PARAMETER, msg);
    }

    // RSQL syntax sai (ví dụ: price=gt=abc, thiếu dấu bằng, phép so sánh không hỗ trợ)
    @ExceptionHandler(RSQLParserException.class)
    public ResponseEntity<APIResponse<Void>> handleRsql(RSQLParserException ex) {
        return build(HttpStatus.BAD_REQUEST, APIResponseStatus.INVALID_PARAMETER,
                    "Invalid RSQL query syntax: " + ex.getMessage());
    }

    // 403 Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, APIResponseStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleAny(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                     APIResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<APIResponse<Void>> build(@NonNull HttpStatus http, APIResponseStatus code, String msg) {
        return ResponseEntity.status(http)
                .body(new APIResponse<>(new APIResponseHeader(code, msg), null));
    }
}
