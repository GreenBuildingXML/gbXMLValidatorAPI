package com.bimport.asharea.mySQL.user;

import com.bimport.asharea.common.hash.HashMethod;
import com.bimport.asharea.common.hash.Hasher;
import com.bimport.asharea.mySQL.user.model.User;
import com.bimport.asharea.mySQL.user.model.UserInfo;
import com.bimport.asharea.mySQL.user.model.UserInfoRepository;
import com.bimport.asharea.mySQL.user.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class UserDAO {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    UserRepository userRepo;

    @Autowired
    UserInfoRepository userInfoRepo;

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

    public UserInfo searchUserByEmail(String email) {
        return userInfoRepo.findTopByEmail(email.toLowerCase());
    }

    public UserInfo getUserInfoById(String userId){
        return userInfoRepo.findById(Long.parseLong(userId)).orElse(null);
    }

    public UserInfo saveUserInfo(UserInfo userInfo){
        return userInfoRepo.saveAndFlush(userInfo);
    }

}
