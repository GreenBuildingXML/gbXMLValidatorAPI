package com.bimport.ashrae.common;

import com.bimport.ashrae.common.hash.HashMethod;
import com.bimport.ashrae.common.hash.Hasher;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class SecurityUtil {
    private final static Random RAND = new Random(System.currentTimeMillis());

    public static String genSessionId(String userId) {
        String sb = userId +
                System.currentTimeMillis() +
                RAND.nextLong();

        return Hasher.hash(sb, HashMethod.SHA256);
    }

    public static String genSalt() {
        return Hasher.hash(String.valueOf(RAND.nextLong()), HashMethod.MD5);
    }

    public static String genRandomStr() {
        return Hasher.hash(String.valueOf(RAND.nextLong()), HashMethod.SHA256);
    }

    public static String genSaltedHash(String str, String salt) {
        return Hasher.hash(str + salt, HashMethod.MD5);
    }

    /**
     * [0] is salt, [1] is hashed password
     */
    public static String[] genSaltAndPassword(String password) {
        String salt = Hasher.hash(String.valueOf(RAND.nextInt()), HashMethod.MD5);
        String passwordSaltHash = Hasher.hash(password + salt, HashMethod.MD5);

        return new String[]{salt, passwordSaltHash};
    }

    public static String getURLRandomStr(int len) {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        return RandomStringUtils.random(len, characters);
    }

    public static void main(String[] args) {
        System.out.println(getURLRandomStr(30));
    }
}
