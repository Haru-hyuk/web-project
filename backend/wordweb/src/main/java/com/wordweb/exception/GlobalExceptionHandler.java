package com.wordweb.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.wordweb.dto.common.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** ============================
     *   JWT 만료 / 유효성 오류
     * ============================ */
    @ExceptionHandler({ExpiredJwtException.class, JwtException.class})
    public ResponseEntity<ErrorResponse> handleJwtException(Exception e) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
    }

    /** ============================
     *   IllegalArgumentException
     * ============================ */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        System.err.println("IllegalArgumentException 발생: " + e.getMessage());
        e.printStackTrace();
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, msg);
    }
    
    /** ============================
     *   JSON 파싱 오류 (잘못된 요청 형식)
     * ============================ */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        String msg = "요청 데이터 형식이 올바르지 않습니다. JSON 형식을 확인해주세요.";
        System.err.println("JSON 파싱 오류: " + e.getMessage());
        e.printStackTrace();
        return buildResponse(HttpStatus.BAD_REQUEST, msg);
    }

    /** ============================
     *   RuntimeException
     * ============================ */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        System.err.println("RuntimeException 발생: " + e.getMessage());
        e.printStackTrace();
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** ============================
     *   그 외 모든 예외
     * ============================ */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }


    /** ============================
     *   공통 Response 생성 함수
     * ============================ */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(
                        ErrorResponse.builder()
                                .success(false)
                                .message(message)
                                .status(status.value())
                                .build()
                );
    }
}
