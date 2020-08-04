package com.bimport.asharea.common.hash;

import com.bimport.asharea.common.StringUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    /**
     * method supports:<br/>
     * <ul>
     * <li>MD5: 32 letters</li>
     * <li>SHA1: 40 letters</li>
     * <li>SHA256: 64 letters</li>
     * </ul>
     *
     * @param str    string to be hashed
     * @param method hash method
     * @return hased string
     */
    public static String hash(String str, HashMethod method) {
        byte[] digest = digestBytes(str, method);
        if (digest == null) {
            return str;
        }

        return encodeBytes(digest);
    }

    public static String hash(String str, HashMethod method, int maxLen) {
        byte[] digest = digestBytes(str, method);
        if (digest == null) {
            return str;
        }

        byte[] folded = foldBytes(digest, maxLen / 2);
        return encodeBytes(folded);
    }

    private static byte[] digestBytes(String str, HashMethod method) {
        if (StringUtil.isNullOrEmpty(str)) {
            return null;
        }

        MessageDigest mdAlgorithm;
        try {
            mdAlgorithm = MessageDigest.getInstance(method.getMethod());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        mdAlgorithm.update(str.getBytes());
        return mdAlgorithm.digest();
    }

    private static String encodeBytes(byte[] digest) {
        StringBuilder hexString = new StringBuilder();

        String str;
        for (int i = 0; i < digest.length; i++) {
            str = Integer.toHexString(0xFF & digest[i]);
            if (str.length() < 2) {
                str = "0" + str;
            }
            hexString.append(str);
        }
        return hexString.toString();
    }

    private static byte[] foldBytes(byte[] bytes, int maxLen) {
        byte[] newBytes = bytes;

        int len = bytes.length;
        while (len > maxLen) {
            int newLen = (len + 1) / 2;

            newBytes = new byte[newLen];
            for (int i = 0, j = len - 1; i <= j; i++, j--) {
                if (i != j) {
                    newBytes[i] = (byte) (0xff & (bytes[i] ^ bytes[j]));
                } else {
                    newBytes[i] = bytes[i];
                }
            }
            len = newLen;
            bytes = newBytes;
        }
        return newBytes;
    }
}
