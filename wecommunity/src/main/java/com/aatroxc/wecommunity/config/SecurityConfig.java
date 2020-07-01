package com.aatroxc.wecommunity.config;

import com.aatroxc.wecommunity.model.enums.UserType;
import com.aatroxc.wecommunity.model.support.BaseResponse;
import com.aatroxc.wecommunity.utils.JsonUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author mafei007
 * @date 2020/5/14 22:07
 */

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		String[] allLoginAuthorities = Stream.of(UserType.values())
				.map(Enum::name)
				.toArray(String[]::new);

		// 授权
		http.authorizeRequests()
				.antMatchers(
						"/user/setting",
						"/user/upload",
						"/user/header/url",
						"/user/reply",
						"/comment/add/**",
						"/letter/**",
						"/notice/**",
						"/like",
						"/follow",
						"/unfollow",
						"/discuss/delete"
				).hasAnyAuthority(allLoginAuthorities)
				.antMatchers(HttpMethod.POST,  "/discuss")
				.hasAnyAuthority(allLoginAuthorities)
				.antMatchers(HttpMethod.PUT, "/discuss")
				.hasAnyAuthority(allLoginAuthorities)
				.antMatchers(
						"/discuss/top",
						"/discuss/wonderful"
				).hasAnyAuthority(UserType.MODERATOR.name())
				.antMatchers(
						"/discuss/restore",
						"/data/**",
						"/actuator/**")
				.hasAnyAuthority(UserType.ADMIN.name())
				.anyRequest().permitAll()
				.and().csrf().disable();

		// 权限不足时的处理
		http.exceptionHandling()
				.authenticationEntryPoint(new AuthenticationEntryPoint() {
					// 没有登陆
					@Override
					public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
						String loginUrl = request.getContextPath() + "/login";

						// 判断是否 ajax 请求
						String xRequestedWith = request.getHeader("x-requested-with");
						if ("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)) {
							// ajax请求的话就响应 403 json
							int code = HttpStatus.FORBIDDEN.value();
							response.setStatus(code);
							response.setContentType("application/json;charset=utf-8");
							BaseResponse<String> resp = new BaseResponse<>(code, HttpStatus.FORBIDDEN.getReasonPhrase(), loginUrl);
							String json = JsonUtils.objectToJson(resp);
							response.getWriter().write(json);
						} else {
							// 其它请求方式就直接响应302重定向
							response.sendRedirect(loginUrl);
						}
					}
				})
				.accessDeniedHandler(new AccessDeniedHandler() {
					// 已经登陆了，但权限不足
					@Override
					public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
						// 判断是否 ajax 请求
						String xRequestedWith = request.getHeader("x-requested-with");
						if ("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)) {
							// 401
							int code = HttpStatus.UNAUTHORIZED.value();
							response.setStatus(code);
							response.setContentType("application/json;charset=utf-8");
							BaseResponse<String> resp = new BaseResponse<>(code, "您没有访问此功能的权限！", null);
							String json = JsonUtils.objectToJson(resp);
							response.getWriter().write(json);
						} else {
							// 重定向到权限不足的页面
							// response.sendRedirect(request.getContextPath() + "/denied");
							// 重定向地址会变成 /denied，这里考虑使用转发
							request.getRequestDispatcher("/denied").forward(request, response);
						}
					}
				});

		// Security 底层 filter 会拦截 /logout路径，进行退出处理，拦截后就不往后执行了
		// 覆盖它默认的逻辑，才能执行我们自己的退出代码
		// 就是让它的拦截一个不存在的路径，由我们自己实现退出
		http.logout().logoutUrl("/securitylogout");

	}
}
