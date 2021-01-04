package com.bimport.ashrae.mySQL.user;

import com.bimport.ashrae.common.Base64Compression;
import com.bimport.ashrae.common.SecurityUtil;
import com.bimport.ashrae.common.hash.HashMethod;
import com.bimport.ashrae.common.hash.Hasher;
import com.bimport.ashrae.mySQL.user.model.User;
import com.bimport.ashrae.mySQL.user.model.UserInfo;
import com.bimport.ashrae.mySQL.user.model.UserInfoRepository;
import com.bimport.ashrae.mySQL.user.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
public class UserDAO {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    UserRepository userRepo;

    @Autowired
    UserInfoRepository userInfoRepo;

    @Autowired
    Base64Compression base64Compression;

    private User getUserLoginInfoHashedUsername(String hash) {
        return userRepo.findFirstByHashUsernameAndIsActive(hash, true);
    }

    public User getUserLoginInfoUsername(String username) {
        return getUserLoginInfoHashedUsername(Hasher.hash(username.toLowerCase(), HashMethod.MD5));
    }
    public User getUserByUsername(String username) {
        return userRepo.findUserByUsername(username);
    }

    public User updateUserAccount(User userAccount) {
        return userRepo.saveAndFlush(userAccount);
    }

    public User getUserById(String userId) {
        return userRepo.findById(Long.parseLong(userId)).orElse(null);
    }

    public User saveUser(User user) {
        return userRepo.saveAndFlush(user);
    }

    public User saveUser(User user, UserInfo userInfo) {
        userInfo.setUser(user);
        user.setUserInfo(userInfo);
        user = userRepo.save(user);
        userInfoRepo.save(userInfo);
        return user;
    }

    public void updateUserProfileImage(String userId, File image){
        byte[] compressed = base64Compression.compressFileBase64(image);
        UserInfo userInfo = getUserInfoById(userId);
        userInfo.setPicBlob(compressed);
        userInfoRepo.saveAndFlush(userInfo);
    }

    public UserInfo updateUserInfo(String userId, String firstName, String lastName, String org, String phone){
        UserInfo old_userinfo = getUserInfoById(userId);
        old_userinfo.setFirstName(firstName);
        old_userinfo.setLastName(lastName);
        old_userinfo.setOrganization(org);
        old_userinfo.setPhone(phone);
        return userInfoRepo.saveAndFlush(old_userinfo);
    }

    public UserInfo searchUserByEmail(String email) {
        return userInfoRepo.findTopByEmail(email.toLowerCase());
    }

    public UserInfo getUserInfoById(String userId){
        return userInfoRepo.findById(Long.parseLong(userId)).orElse(null);
    }

    public Boolean validatePassword(User user, String password) {
        String hashPwd = user.getPassword();
        String salt = user.getSalt();
        String saltedPwd = SecurityUtil.genSaltedHash(password, salt);
        if (saltedPwd.equals(hashPwd)) {
            return true;
        }
        return false;
    }

    public UserInfo saveUserInfo(UserInfo userInfo){
        return userInfoRepo.saveAndFlush(userInfo);
    }

}
