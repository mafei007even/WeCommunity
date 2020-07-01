package com.aatroxc.wecommunity.model.enums;

/**
 * 对 {@link CommentEntityType} 的点赞状态
 *
 * @author mafei007
 * @date 2020/4/19 22:45
 */


public enum LikeStatus implements ValueEnum<Integer> {

	/**
	 * 点赞
	 */
	LIKE(1),

	/**
	 * 踩
	 */
	DISLIKE(-1),

	/**
	 * 没有点赞也没有踩
	 */
	NONE(0);

	private final Integer value;

	LikeStatus(Integer value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}
}
