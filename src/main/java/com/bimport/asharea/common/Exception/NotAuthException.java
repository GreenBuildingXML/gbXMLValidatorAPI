package com.bimport.asharea.common.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED,reason="Username or Password is wrong")
public class NotAuthException extends RuntimeException {
    public NotAuthException() {
    }

    public NotAuthException(String message) {
        super(message);
    }


}

