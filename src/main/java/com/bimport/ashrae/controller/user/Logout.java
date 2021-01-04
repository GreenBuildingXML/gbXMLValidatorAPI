package com.bimport.ashrae.controller.user;

import com.bimport.ashrae.common.ServletUtil;
import com.bimport.ashrae.common.StringUtil;
import com.bimport.ashrae.common.WebUtil;
import com.bimport.ashrae.common.redis.RedisAccess;
import com.bimport.ashrae.security.LoginValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class Logout {
    @Autowired
    RedisAccess redisAccess;
    @Autowired
    ServletUtil servletUtil;


    @RequestMapping(path = "/Logout",  method = RequestMethod.POST)
    @ResponseBody
    protected void LogOut(HttpServletRequest req, HttpServletResponse resp){
        String session = WebUtil.readCookie(req, LoginValidator.SESSION_COOKIE_NAME);
        if(!StringUtil.isNullOrEmpty(session)){
            redisAccess.del(session);
        }
        servletUtil.returnString(resp, "");
    }
}
