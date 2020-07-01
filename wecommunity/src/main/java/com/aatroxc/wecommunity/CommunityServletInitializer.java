package com.aatroxc.wecommunity;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 部署到 tomcat， 会访问 SpringBootServletInitializer.configure() 方法
 * 作为入口来运行这个项目，要指定核心配置文件
 *
 * @author mafei007
 * @date 2020/5/22 0:20
 */


public class CommunityServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(CommunityApplication.class);
	}
}
