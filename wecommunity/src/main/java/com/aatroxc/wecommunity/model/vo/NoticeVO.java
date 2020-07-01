package com.aatroxc.wecommunity.model.vo;

import com.aatroxc.wecommunity.model.entity.Message;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import lombok.Data;

/**
 * 通知详情页 vo，放在 List 中
 * @author mafei007
 * @date 2020/5/8 16:47
 */

@Data
public class NoticeVO {

	private Message notice;

	/**
	 * 触发这条通知的用户
	 */
	private User user;

	/**
	 * 就是 id 为 1 的系统管理员
	 */
	private User fromUser;

	/**
	 * 触发事件通知的类型
	 */
	private CommentEntityType entityType;

	/**
	 * 触发的事件通知的id，可能是帖子id、评论的id..
	 */
	private Integer entityId;

	/**
	 * 如果是通知是： (点赞、评论)，此字段有值，是要跳转的帖子id
	 * 关注通知没有此字段
	 */
	private Integer postId;

}
