package com.bimport.ashrae.security;

import com.bimport.ashrae.common.ServletUtil;
import com.bimport.ashrae.common.StringUtil;
import com.bimport.ashrae.common.WebUtil;
import com.bimport.ashrae.common.redis.RedisAccess;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoggedInValidator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisAccess redisAccess;

    @Autowired
    ServletUtil servletUtil;

    String validate(HttpServletRequest req) {
        String sessionId = WebUtil.readCookie(req, LoginValidator.SESSION_COOKIE_NAME);
        logger.info("sessionId: " + sessionId);
        if (sessionId == null) {
            sessionId = req.getHeader("Authorization");

            if (sessionId == null) {
                sessionId = req.getHeader("authorization");
            }
        }

        String userId = null;
        if (!StringUtil.isNullOrEmpty(sessionId)) {
            userId = redisAccess.get(sessionId);
        }
        return userId;
    }

    boolean filter(String userId, HttpServletResponse resp) {
        if (userId != null) {
            return true;
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("status", "redirect");
            res.addProperty("url", "/login");
            servletUtil.returnJsonResult(resp, res);
            return false;
        }
    }
}
