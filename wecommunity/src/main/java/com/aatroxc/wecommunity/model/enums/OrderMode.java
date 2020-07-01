package com.aatroxc.wecommunity.model.enums;

import lombok.AllArgsConstructor;

/**
 * 排序模式，给主页用的
 * 0-按日期排序
 * 1-按热度排序
 *
 * @author mafei007
 * @date 2020/5/18 22:18
 */

@AllArgsConstructor
public enum OrderMode implements ValueEnum<Integer> {

	DATE(0),

	HEAT(1);

	private final Integer value;

	@Override
	public Integer getValue() {
		return value;
	}
}
