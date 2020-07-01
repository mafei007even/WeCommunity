package com.aatroxc.wecommunity.model.enums;

import java.util.concurrent.TimeUnit;

/**
 * 用户登陆的过期时间
 *
 * @author mafei007
 * @date 2020/4/2 19:15
 */


public enum ExpiredTime {

    /**
     * 默认状态的登陆凭证的过期时间
     */
    DEFAULT_EXPIRED(12, TimeUnit.HOURS),

    /**
     * 记住状态的登陆凭证超时时间
     */
    REMEMBER_EXPIRED(90 * 24, TimeUnit.HOURS);

    private final int timeout;
    private final TimeUnit timeUnit;

    ExpiredTime(int timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public int getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
