package com.bimport.asharea.common;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailUtil {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public boolean isEmailValid(String email) {
        if (!StringUtil.isNullOrEmpty(email)) {
            return EmailValidator.getInstance(false, true).isValid(email);
        }
        return false;
    }
}
