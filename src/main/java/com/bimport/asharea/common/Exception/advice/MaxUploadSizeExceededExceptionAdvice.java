package com.bimport.asharea.common.Exception.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;


@RestControllerAdvice
public class MaxUploadSizeExceededExceptionAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CustomErrorResponse> handleMaxUploadSizeExceededException(HttpServletResponse resp) throws IOException {
        CustomErrorResponse error = new CustomErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setMessage("The file size exceeds the 5MB limit");
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
