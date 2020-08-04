package com.bimport.asharea.common;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebUtil {
    public static void setCookie(HttpServletResponse resp, String key, String value, int maxAge){
        Cookie sessionCookie = new Cookie(key, value);
        sessionCookie.setMaxAge(maxAge);
        resp.addCookie(sessionCookie);
    }

    public static String readCookie(HttpServletRequest httpReq, String cookieKey){
         Cookie[] cookies = httpReq.getCookies();
         String sessionID = null;
         if(cookies != null){
            for(Cookie c : cookies){
                if(c.getName().equals(cookieKey)){
                    sessionID = c.getValue().trim();
                    break;
                }
            }
         }
         return sessionID;
    }
}
