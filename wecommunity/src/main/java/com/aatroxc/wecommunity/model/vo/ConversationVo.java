package com.aatroxc.wecommunity.model.vo;

import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.entity.Message;
import lombok.Data;

/**
 *
 * 进入私信页面，需要会话、会话未读数量、所有数量
 *
 * @author mafei007
 * @date 2020/4/18 17:38
 */

@Data
public class ConversationVo {

	/**
	 * 会话的最后一条消息
	 */
	private Message conversation;
	/**
	 * 会话的总消息数
	 */
	private int letterCount;
	/**
	 * 会话未读消息数
	 */
	private int unreadCount;
	/**
	 * 会话的目标用户
	 */
	private User target;

}
