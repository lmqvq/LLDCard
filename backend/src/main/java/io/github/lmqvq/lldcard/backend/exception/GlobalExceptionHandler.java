package io.github.lmqvq.lldcard.backend.exception;

import io.github.lmqvq.lldcard.backend.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<LoginResponse> handleRuntimeException(RuntimeException exception) {
        logger.warn("Request processing failed: {}", exception.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(LoginResponse.error("请求处理失败"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<LoginResponse> handleException(Exception exception) {
        logger.error("Unhandled request failure", exception);
        return ResponseEntity.internalServerError().body(LoginResponse.error("系统错误，请稍后重试"));
    }
}