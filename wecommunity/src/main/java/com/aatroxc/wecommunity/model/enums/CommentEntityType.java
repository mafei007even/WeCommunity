package com.aatroxc.wecommunity.model.enums;

/**
 * comment 评论表中 entity_type 字段
 * 就是当前一条记录是属于帖子下的直接评论，还是评论的评论
 * 可以扩展出更多别的类型的评论
 * : 课程的评论...
 *
 *
 * 此枚举根据业务的增加，应该重命名为: EntityType
 *
 * @author mafei007
 * @date 2020/4/9 16:29
 */


public enum CommentEntityType implements ValueEnum<Integer> {

    /**
     * 代表直接对帖子进行评论操作或点赞操作
     */
    POST(1),

    /**
     * 代表对帖子下的评论进行评论操作或点赞操作
     */
    COMMENT(2),

    /**
     * 代表操作的实体对象是用户，使用在关注功能中.
     */
    USER(3);


    private final Integer value;

    CommentEntityType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }


}
