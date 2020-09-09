package com.bimport.asharea.common.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public class RecaptchaValidateException extends RuntimeException {
    public RecaptchaValidateException(String message) {
        super(message);
    }
}
