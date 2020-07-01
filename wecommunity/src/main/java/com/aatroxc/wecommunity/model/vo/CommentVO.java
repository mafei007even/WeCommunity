package com.aatroxc.wecommunity.model.vo;

import com.aatroxc.wecommunity.model.entity.Comment;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.enums.LikeStatus;
import lombok.Data;

import java.util.List;

/**
 *
 * 评论的vo
 *
 * @author mafei007
 * @date 2020/4/9 17:00
 */

@Data
public class CommentVO {

    private Comment comment;
    private User user;
    private long likeCount;
    private LikeStatus likeStatus;
    private List<ReplyVO> replys;
    private Integer replyCount;

}
