package com.aatroxc.wecommunity.utils;

import org.springframework.util.Assert;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mafei007
 * @date 2020/4/3 23:20
 */


public class CookieUtils {

    public static String getValue(HttpServletRequest request, String name) {
        Assert.notNull(request, "request参数不能为null");
        Assert.notNull(request, "name参数不能为null");

        Cookie[] cookies = request.getCookies();
        if (cookies == null){
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
