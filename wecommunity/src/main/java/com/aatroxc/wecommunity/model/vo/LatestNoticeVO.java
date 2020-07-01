package com.aatroxc.wecommunity.model.vo;

import com.aatroxc.wecommunity.model.entity.Message;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import lombok.Data;

/**
 * 进入系统通知页面，需要显示（点赞、评论、关注）的最新一条消息
 * @author mafei007
 * @date 2020/5/6 17:54
 */

@Data
public class LatestNoticeVO {

	/**
	 * （点赞或评论或关注）中的最新一条消息
	 */
	private Message message;

	/**
	 * 触发这条通知的用户
	 */
	private User user;

	/**
	 * 触发事件通知的类型
	 */
	private CommentEntityType entityType;

	/**
	 * 用户某个系统通知（点赞、评论、关注）下的总通知数量
	 */
	private Integer count;

	/**
	 * 用户某个系统通知（点赞、评论、关注）未读通知的数量
	 */
	private Integer unread;

}
