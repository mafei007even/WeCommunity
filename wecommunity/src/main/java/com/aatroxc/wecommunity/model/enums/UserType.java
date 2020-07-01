package com.aatroxc.wecommunity.model.enums;

/**
 * @author mafei007
 * @date 2020/4/7 22:53
 */


public enum UserType implements ValueEnum<Integer> {

    /**
     * 普通用户
     */
    ORDINARY(0),

    /**
     * 管理员
     */
    ADMIN(1),

    /**
     * 系统版主
     */
    MODERATOR(2);


    private final Integer value;

    UserType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
