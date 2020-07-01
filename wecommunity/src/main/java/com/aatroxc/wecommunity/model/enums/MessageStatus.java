package com.aatroxc.wecommunity.model.enums;

/**
 * 私信消息的状态
 *
 * @date 2020/5/11 19:13
 * DELETED 用于系统通知消息的删除
 * 私信消息的删除使用 deleteBy字段
 *
 * @author mafei007
 * @date 2020/4/18 15:12
 */


public enum MessageStatus implements ValueEnum<Integer> {

	/**
	 * 未读
	 */
	UNREAD(0),

	/**
	 * 以读
	 */
	READED(1),

	/**
	 * 删除
	 */
	DELETED(2);

	private Integer value;

	MessageStatus(Integer value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}
}
