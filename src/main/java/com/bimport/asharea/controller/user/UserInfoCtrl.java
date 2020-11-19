package com.bimport.asharea.controller.user;
import com.bimport.asharea.common.Exception.ConflictException;
import com.bimport.asharea.common.FileUtil;
import com.bimport.asharea.common.ImageUtil;
import com.bimport.asharea.common.StringUtil;
import com.bimport.asharea.mySQL.user.UserDAO;
import com.bimport.asharea.mySQL.user.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserInfoCtrl {
    @Autowired
    UserDAO userDAO;
    @Autowired
    FileUtil fileUtil;

    @RequestMapping(path = "/GetUserInfo", method = RequestMethod.GET)
    public UserInfo GetUserInfo(HttpServletRequest req, String userId) {
        if (StringUtil.isNullOrEmpty(userId)) {
            userId = (String) req.getAttribute("userId");
        }
        UserInfo info = userDAO.getUserInfoById(userId);
        return info;
    }

    @RequestMapping(value = "/UploadProfileImage", method = RequestMethod.POST)
    public String UploadProfileImage(HttpServletRequest req) throws ServletException, IOException {
        String userId = (String) req.getAttribute("userId");
        Part part = req.getPart("image");
        String filename = part.getSubmittedFileName();
        File file = fileUtil.convertInputStreamToFile(part.getInputStream(), filename);

        if (!ImageUtil.cropImageToSquare(file, 500)) {
            throw new ConflictException("Parse user profile image failed");
        }

        userDAO.updateUserProfileImage(userId, file);

        return FileUtil.BASE64_PREFIX + fileUtil.encodeToBase64Str(file);
    }

    @RequestMapping(path = "/UpdateUserInfo", method = RequestMethod.PUT)
    @ResponseBody
    public UserInfo UpdateUserInfo(HttpServletRequest req, @RequestParam String firstName, @RequestParam String lastName, @RequestParam String organization, @RequestParam String phone) {
        String userId = (String) req.getAttribute("userId");
        return userDAO.updateUserInfo(userId, firstName, lastName, organization, phone);
    }
}
