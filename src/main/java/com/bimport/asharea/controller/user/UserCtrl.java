package com.bimport.asharea.controller.user;

import com.bimport.asharea.common.*;
import com.bimport.asharea.common.Exception.ConflictException;
import com.bimport.asharea.common.Exception.NotFoundException;
import com.bimport.asharea.common.hash.HashMethod;
import com.bimport.asharea.common.hash.Hasher;
import com.bimport.asharea.common.redis.RedisAccess;
import com.bimport.asharea.mySQL.user.UserDAO;
import com.bimport.asharea.mySQL.user.model.User;
import com.bimport.asharea.mySQL.user.model.UserInfo;
import com.bimport.asharea.security.LoginValidator;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class UserCtrl {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisAccess redisAccess;
    @Autowired
    ServletUtil servletUtil;
    @Autowired
    EmailUtil emailUtil;
    @Autowired
    UserDAO userDAO;


    @RequestMapping(path = "/ActivateUser", method = RequestMethod.PUT)
    @ResponseBody
    public void ActivateUser(HttpServletResponse resp, @RequestParam String uuid) {
        JsonObject res = new JsonObject();
        String record = redisAccess.get(uuid);
        if (StringUtil.isNullOrEmpty(record)) {
            servletUtil.returnErrorResult(resp, res, "Invalid request, cannot find request record");
            return;
        }

        JsonObject jo = JsonUtil.parseToJsonObject(record);
        if (jo == null) {
            servletUtil.returnErrorResult(resp, res, "Invalid request, data corrupted");
            return;
        }

        String userId = JsonUtil.readStringValue(jo, "user_id", null);
        logger.info("user id: " + userId);
        User userAccount;
        if (userId != null) {
            userAccount = userDAO.getUserById(userId);
            if (userAccount == null) {
                servletUtil.returnErrorResult(resp, res, "Invalid request, cannot find user data");
                return;
            }
        } else {
            userAccount = new User();
        }

        userAccount.setIsActive(true);
        UserInfo userInfo = userDAO.getUserInfoById(userId);
        userInfo.setValidated(true);

        try {
            userAccount = userDAO.saveUser(userAccount);
            userDAO.saveUserInfo(userInfo);
            userId = String.valueOf(userAccount.getId());
        } catch (DataIntegrityViolationException ex) {
            servletUtil.returnErrorResult(resp, res, "Save user account failed: " + ex.getMessage());
            return;
        }

        String sessionId = SecurityUtil.genSessionId(userId);
        redisAccess.set(sessionId, userId, LoginValidator.SESSION_LIFE);
        WebUtil.setCookie(resp, LoginValidator.SESSION_COOKIE_NAME, sessionId, -1);

        redisAccess.del(uuid);
        res.addProperty("status", "success");
        servletUtil.returnJsonResult(resp, res);
    }

    @ResponseBody
    @RequestMapping(value = "/Register", method = RequestMethod.POST)
    public String Register(@RequestParam String username, @RequestParam String password, @RequestBody UserInfo user) {
        String email = user.getEmail();
        logger.info("email: "  + email);
        if (StringUtil.isNullOrEmpty(email)) {
            throw new NotFoundException("Email is missing");
        }

        if (!emailUtil.isEmailValid(email)) {
            throw new ConflictException("Email is not valid");
        }

        if (userDAO.searchUserByEmail(email) != null) {
            throw new ConflictException("Email is already used, please use another one");
        }
        User userAccount = new User();
        String salt = SecurityUtil.genSalt();
        String hashUserName = Hasher.hash(username.toLowerCase(), HashMethod.MD5);
        String hashPwd = SecurityUtil.genSaltedHash(password, salt);
        userAccount.setSalt(salt);
        userAccount.setUsername(username);
        userAccount.setHashUsername(hashUserName);
        userAccount.setPassword(hashPwd);
        userAccount.setIsActive(false);

        user.setValidated(false);

        userAccount = userDAO.saveUser(userAccount, user);

        //generate uuid and store the inviter id and sender id
        String uuid = SecurityUtil.getURLRandomStr(20);
        JsonObject record = new JsonObject();
        record.addProperty("user_id", userAccount.getId());
        redisAccess.set(uuid, record.toString());

        //todo send activation email to user


        return uuid;
    }

}
