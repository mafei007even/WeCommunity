package com.aatroxc.wecommunity.model.enums;

/**
 * 账号激活的状态
 * @author mafei007
 * @date 2020/4/1 23:34
 */


public enum UserActivationStatus implements ValueEnum<Integer>{

    /**
     * 数据库值 未激活 0
     */
    NOT_ACTIVED(0),

    /**
     * 数据库值 已激活 1
     */
    ACTIVED(1),

    /**
     * 业务值，代表已经激活了再次激活，
     * 重复激活 2
     */
    REPEAT(2),

    /**
     * 业务值，代表激活码不正确，激活失败 3
     */
    FAILURE(3);

    private final Integer valaue;

    UserActivationStatus(Integer valaue) {
        this.valaue = valaue;
    }

    @Override
    public Integer getValue() {
        return valaue;
    }
}
