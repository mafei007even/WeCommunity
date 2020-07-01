package com.aatroxc.wecommunity.model.enums;

/**
 *
 * 帖子的类型
 * @author mafei007
 * @date 2020/4/7 22:21
 */


public enum DiscussPostType implements ValueEnum<Integer> {

    /**
     * 普通类型的帖子
     */
    ORDINARY(0),

    /**
     * 置顶帖
     */
    STICK(1);

    private final Integer value;

    DiscussPostType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
