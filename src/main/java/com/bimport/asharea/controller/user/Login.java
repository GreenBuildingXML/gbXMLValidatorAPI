package com.bimport.asharea.controller.user;

import com.bimport.asharea.common.Exception.NotAuthException;
import com.bimport.asharea.common.ServletUtil;
import com.bimport.asharea.common.StringUtil;
import com.bimport.asharea.common.WebUtil;
import com.bimport.asharea.common.hash.HashMethod;
import com.bimport.asharea.common.hash.Hasher;
import com.bimport.asharea.common.redis.RedisAccess;
import com.bimport.asharea.mySQL.user.recaptcha.CaptchaService;
import com.bimport.asharea.mySQL.user.UserDAO;
import com.bimport.asharea.mySQL.user.model.User;
import com.bimport.asharea.security.LoginValidator;
import com.google.gson.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class Login {
    @Autowired
    UserDAO userDAO;
    @Autowired
    RedisAccess redisAccess;
    @Autowired
    ServletUtil servletUtil;
    @Autowired
    CaptchaService captchaService;

    @RequestMapping(path = "/Login", method = RequestMethod.POST)
    @ResponseBody
    protected void Login(@RequestParam String username, @RequestParam String password, String token, HttpServletResponse resp) {
        JsonObject res = new JsonObject();
        res.addProperty("status", "success");
        //todo within mobile app
        captchaService.validateCaptcha(token);

        if (StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new NotAuthException();
        }
        // Case: username login
        User user = userDAO.getUserLoginInfoUsername(username);
        if (user == null) {
            // Case: username should not be case sensitive
            user = userDAO.getUserByUsername(username);
            if (user != null) {
                user.setHashUsername(Hasher.hash(username.toLowerCase(), HashMethod.MD5));
                userDAO.updateUserAccount(user);
            } else {
                throw new NotAuthException();
            }

        }
        JsonObject validate = LoginValidator.validate(user, password);

        // save session info to Redis
        String sessionId = validate.get("session_id").getAsString();
        redisAccess.set(sessionId, user.getId().toString(), LoginValidator.SESSION_LIFE);
        WebUtil.setCookie(resp, LoginValidator.SESSION_COOKIE_NAME, sessionId, -1);
        res.addProperty("token", sessionId);
        servletUtil.returnJsonResult(resp, res);
    }
}
