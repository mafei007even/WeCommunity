package com.aatroxc.wecommunity.aspect;

import cn.hutool.extra.servlet.ServletUtil;
import com.aatroxc.wecommunity.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class ControllerLogAop {

	@Pointcut("execution(*  com.aatroxc.wecommunity.controller.*.*(..))")
	public void controller() {
	}

	@Around("controller()")
	public Object controller(ProceedingJoinPoint joinPoint) throws Throwable {
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		Object[] args = joinPoint.getArgs();

		// Get request attribute
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = Objects.requireNonNull(requestAttributes).getRequest();

		printRequestLog(request, className, methodName, args);
		long start = System.currentTimeMillis();
		Object returnObj = joinPoint.proceed();
		printResponseLog(request, className, methodName, returnObj, System.currentTimeMillis() - start);
		return returnObj;
	}


	private void printRequestLog(HttpServletRequest request, String clazzName, String methodName, Object[] args) throws JsonProcessingException {
		log.debug("Request URL: [{}], URI: [{}], Request Method: [{}], IP: [{}]",
				request.getRequestURL(),
				request.getRequestURI(),
				request.getMethod(),
				ServletUtil.getClientIP(request));

		if (args == null || !log.isDebugEnabled()) {
			return;
		}

		// 过滤掉参数: request, resopnse, Model....  这些参数不需要打印日志
		args = Arrays.stream(args)
				.filter(arg -> !(arg instanceof HttpServletRequest ||
						arg instanceof HttpServletResponse ||
						arg instanceof MultipartFile ||
						arg instanceof Model ||
						arg.getClass().isAssignableFrom(MultipartFile[].class)))
				.toArray();

		String requestBody = JsonUtils.objectToJson(args);
		log.debug("{}.{} Parameters: [{}]", clazzName, methodName, requestBody);
	}

	private void printResponseLog(HttpServletRequest request, String className, String methodName, Object returnObj, long usage) throws JsonProcessingException {
		if (log.isDebugEnabled()) {
			String returnData = "";

			if (returnObj != null) {
				if (returnObj instanceof ResponseEntity) {
					ResponseEntity responseEntity = (ResponseEntity) returnObj;
					if (responseEntity.getBody() instanceof Resource) {
						returnData = "[ BINARY DATA ]";
					} else {
						returnData = toString(responseEntity.getBody());
					}
				} else {
					returnData = toString(returnObj);
				}

			}
			log.debug("{}.{} Response: [{}], usage: [{}]ms", className, methodName, returnData, usage);
		}
	}

	@NonNull
	private String toString(@NonNull Object obj) throws JsonProcessingException {
		Assert.notNull(obj, "Return object must not be null");

		String toString = "";
		if (obj.getClass().isAssignableFrom(byte[].class) && obj instanceof Resource) {
			toString = "[ BINARY DATA ]";
		} else {
			toString = JsonUtils.objectToJson(obj);
		}
		return toString;
	}
}

