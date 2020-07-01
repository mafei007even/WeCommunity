package com.aatroxc.wecommunity.model.support;


import com.aatroxc.wecommunity.model.enums.UserType;
import lombok.Data;

/**
 * 载荷对象
 */
@Data
public class UserInfo {

	private Integer id;

	private String username;

	private String headerUrl;

	/**
	 * 此对象是存放在 redis 中的用户登陆凭证
	 * <p>
	 * status 参数登陆后为 1 ，退出后为 0
	 * <p>
	 * 用户退出时不将登陆凭证从 redis 中删除，要留着来统计数据
	 */
	private Integer status;

	/**
	 * 为 Spring Security 增加
	 */
	private String password;

	/**
	 * 为 Spring Security 增加
	 */
	private UserType type;

}