package com.aatroxc.wecommunity.model.enums;


/**
 * 帖子状态
 *
 * @author mafei007
 * @date 2020/4/6 19:24
 */

public enum DiscussPostStatus implements ValueEnum<Integer> {

    /**
     * 正常
     */
    NORMAL(0),

    /**
     * 精华
     */
    ESSENCE(1),

    /**
     * 拉黑
     */
    BLOCK(2);


    private final Integer value;

    DiscussPostStatus(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
