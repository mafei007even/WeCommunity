package com.aatroxc.wecommunity.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author mafei007
 * @date 2020/4/19 16:44
 */

@Component
@Aspect
@Slf4j
public class ServiceLogAop {

	@Pointcut("execution(*  com.aatroxc.wecommunity.service.*.*(..))")
	public void service() {
	}

	@Before("service()")
	public void before(JoinPoint joinPoint) {
		// 用户[47.103.83.12],在[xxx],访问了[com.aatroxc.wecommunity.service.xxx()].
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		// 没有 kafka 前，service都是由 controller 调用的，是肯定存在 request 请求的
		// 现在消费者中调用了 service，没有经过 http request，所以这里会有空指针
		if (requestAttributes == null) {
			// 如果是 kafka 调用 service，就不记录 ip 了
			String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String target = joinPoint.getSignature().getDeclaringType() + "." + joinPoint.getSignature().getName();
			log.debug(String.format("kafka消费者,在[%s],访问了[%s].", now, target));
			return;
		}
		HttpServletRequest request = Objects.requireNonNull(requestAttributes).getRequest();
		String ip = request.getRemoteHost();
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String target = joinPoint.getSignature().getDeclaringType() + "." + joinPoint.getSignature().getName();

		log.debug(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
	}

}
