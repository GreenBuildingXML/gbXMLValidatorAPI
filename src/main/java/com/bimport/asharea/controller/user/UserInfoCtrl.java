package com.bimport.asharea.controller.user;
import com.bimport.asharea.common.StringUtil;
import com.bimport.asharea.mySQL.user.UserDAO;
import com.bimport.asharea.mySQL.user.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserInfoCtrl {
    @Autowired
    UserDAO userDAO;

    @RequestMapping(path = "/GetUserInfo", method = RequestMethod.GET)
    public UserInfo GetUserInfo(HttpServletRequest req, String userId) {
        if (StringUtil.isNullOrEmpty(userId)) {
            userId = (String) req.getAttribute("userId");
        }
        UserInfo info = userDAO.getUserInfoById(userId);
        return info;
    }
}
