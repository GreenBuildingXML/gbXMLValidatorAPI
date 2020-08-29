package com.bimport.asharea.security;

import com.bimport.asharea.common.ServletUtil;
import com.bimport.asharea.common.StringUtil;
import com.bimport.asharea.common.WebUtil;
import com.bimport.asharea.common.redis.RedisAccess;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoggedInValidator {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisAccess redisAccess;

    @Autowired
    ServletUtil servletUtil;

    String validate(HttpServletRequest req) {
        String sessionId = WebUtil.readCookie(req, LoginValidator.SESSION_COOKIE_NAME);
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
