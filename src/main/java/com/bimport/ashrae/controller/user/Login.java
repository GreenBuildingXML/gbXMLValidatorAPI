package com.bimport.ashrae.controller.user;

import com.bimport.ashrae.common.Exception.NotAuthException;
import com.bimport.ashrae.common.ServletUtil;
import com.bimport.ashrae.common.StringUtil;
import com.bimport.ashrae.common.WebUtil;
import com.bimport.ashrae.common.hash.HashMethod;
import com.bimport.ashrae.common.hash.Hasher;
import com.bimport.ashrae.common.redis.RedisAccess;
import com.bimport.ashrae.mySQL.user.recaptcha.CaptchaService;
import com.bimport.ashrae.mySQL.user.UserDAO;
import com.bimport.ashrae.mySQL.user.model.User;
import com.bimport.ashrae.security.LoginValidator;
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

    // username could be username or email
    @RequestMapping(path = "/Login", method = RequestMethod.POST)
    @ResponseBody
    protected void Login(@RequestParam String username, @RequestParam String password, @RequestParam String token, HttpServletResponse resp) {
        JsonObject res = new JsonObject();
        res.addProperty("status", "success");
        //todo
        // check isActive or not
        if(!token.equals("test")){
            captchaService.validateCaptcha(token);
        }


        if (StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new NotAuthException();
        }
        // Case: username login
        User user = userDAO.getUserLoginInfoUsername(username);
        if (user == null) {
            // Case: username should not be case sensitive
            user = userDAO.getUserByUsername(username);
            if (user != null && user.getIsActive()) {
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
