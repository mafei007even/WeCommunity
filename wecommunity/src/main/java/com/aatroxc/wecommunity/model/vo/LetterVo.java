package com.aatroxc.wecommunity.model.vo;

import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.entity.Message;
import lombok.Data;

/**
 * 用户私信的vo，应该是个List，包含双方多个私信消息
 * Letter 是私信的意思
 *
 * 每条私信除了Message，还需要 User，来显示每条消息的用户名和头像
 *
 * @author mafei007
 * @date 2020/4/18 18:48
 */

@Data
public class LetterVo {

	/**
	 * 私信
	 */
	private Message letter;

	/**
	 * 私信的发送者
	 */
	private User fromUser;

}
