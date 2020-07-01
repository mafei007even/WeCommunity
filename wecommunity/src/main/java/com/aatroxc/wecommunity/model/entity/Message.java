package com.aatroxc.wecommunity.model.entity;

import com.aatroxc.wecommunity.model.enums.MessageStatus;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author mafei007
 * @date 2020/4/18 15:08
 */

@Data
@Table(name = "message")
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer fromId;
	private Integer toId;
	private String conversationId;
	private String content;

	/**
	 * 通知类消息使用此字段标记是否删除
	 */
	private MessageStatus status;

	/**
	 * 私信类消息使用此字段标记是否删除
	 * 消息被fromId删了还是toId删了，删除的那一方不显示此条消息
	 * 存的是： A123AA1234A
	 * userId 前后加上 A，避免 id 被另一个 id 包含的现象
	 */
	private String deleteBy;
	private Date createTime;

}
