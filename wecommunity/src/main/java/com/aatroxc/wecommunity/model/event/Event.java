package com.aatroxc.wecommunity.model.event;

import com.aatroxc.wecommunity.model.enums.Topic;
import com.aatroxc.wecommunity.model.enums.CommentEntityType;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mafei007
 * @NoArgsConstructor(access = AccessLevel.PRIVATE)
 * @AllArgsConstructor(access = AccessLevel.PRIVATE)
 * 解决lombok使用Builder构建器，jackson不能反序列化
 * @date 2020/5/5 18:41
 */

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Event {

	/**
	 * 消息的主题
	 */
	private Topic topic;

	/**
	 * 事件是谁触发的
	 */
	private Integer userId;

	/**
	 * 触发的是哪种事件
	 */
	private CommentEntityType entityType;

	/**
	 * 触发的事件的id，可能是帖子id、评论的id..
	 */
	private Integer entityId;

	/**
	 * 触发事件对应的用户，就是要通知的用户
	 */
	private Integer entityUserId;

	/**
	 * 其它不确定的数据存到 map 中
	 * Builder.Default 标识此字段不通过 Buidler模式生成
	 */
	@Builder.Default
	private Map<String, Object> data = new HashMap<>();

	public Event setData(String key, Object value) {
		this.data.put(key, value);
		return this;
	}

}
