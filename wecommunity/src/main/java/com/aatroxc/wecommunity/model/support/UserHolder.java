package com.aatroxc.wecommunity.model.support;



/**
 * @author mafei007
 * @date 2020/4/3 23:56
 */
public class UserHolder {

    private static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    public static void set(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    public static UserInfo get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

}
