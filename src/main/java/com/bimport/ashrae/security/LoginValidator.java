package com.bimport.ashrae.security;

import com.bimport.ashrae.common.Exception.NotAuthException;
import com.bimport.ashrae.common.SecurityUtil;
import com.bimport.ashrae.mySQL.user.model.User;
import com.google.gson.JsonObject;

public class LoginValidator {

    public static final String SESSION_COOKIE_NAME = "asharea_session_id";
    public static final int SESSION_LIFE = 3*60*60; // three hours

    public static JsonObject validate(User info, String password){
        JsonObject res = new JsonObject();
        String hashPwd = info.getPassword();
        String salt = info.getSalt();

        String saltedPwd = SecurityUtil.genSaltedHash(password, salt);

        if(saltedPwd.equals(hashPwd)){
            String userId = info.getId().toString();
            String sessionId = SecurityUtil.genSessionId(userId);
            res.addProperty("session_id", sessionId);
        }else {
            throw new NotAuthException();
        }

        return res;
    }
}
