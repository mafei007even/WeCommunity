package com.aatroxc.wecommunity.controller.interceptor;

import com.aatroxc.wecommunity.annotation.LoginRequired;
import com.aatroxc.wecommunity.model.support.BaseResponse;
import com.aatroxc.wecommunity.model.support.UserHolder;
import com.aatroxc.wecommunity.utils.JsonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author mafei007
 * @date 2020/4/4 23:33
 */

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			Method method = handlerMethod.getMethod();

			// 判断 handler 是否 LoginRequired
			LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
			// 存在 LoginRequired 并且没有登陆
			if (loginRequired != null && UserHolder.get() == null) {
				String loginUrl = request.getContextPath() + "/login";

				// 判断是否 ajax 请求
				String xRequestedWith = request.getHeader("x-requested-with");
				if ("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)) {
					// ajax请求的话就响应 json
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
				return false;
			}
		}

		return true;
	}
}
