package com.aatroxc.wecommunity.config;

import com.aatroxc.wecommunity.controller.interceptor.DataInterceptor;
import com.aatroxc.wecommunity.controller.interceptor.LoginTicketInterceptor;
import com.aatroxc.wecommunity.controller.interceptor.MessageCountInterceptor;
import com.aatroxc.wecommunity.factory.StringToEnumConverterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author mafei007
 * @date 2020/4/3 23:09
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final LoginTicketInterceptor loginTicketInterceptor;
	// private final LoginRequiredInterceptor loginRequiredInterceptor;
	private final MessageCountInterceptor messageCountInterceptor;
	private final DataInterceptor dataInterceptor;

	public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor/*, LoginRequiredInterceptor loginRequiredInterceptor*/, MessageCountInterceptor messageCountInterceptor, DataInterceptor dataInterceptor) {
		this.loginTicketInterceptor = loginTicketInterceptor;
		// this.loginRequiredInterceptor = loginRequiredInterceptor;
		this.messageCountInterceptor = messageCountInterceptor;
		this.dataInterceptor = dataInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(loginTicketInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

		// registry.addInterceptor(loginRequiredInterceptor)
		// 		.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

		registry.addInterceptor(messageCountInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

		registry.addInterceptor(dataInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		// 字符串转枚举工厂
		registry.addConverterFactory(new StringToEnumConverterFactory());
		// 日期字符串转 LocalDate
		registry.addConverter(new Converter<String, LocalDate>() {

			private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			@Override
			public LocalDate convert(String source) {
				return LocalDate.parse(source, formatter);
			}
		});
	}


	/**
	 * 新增枚举转换器
	 *
	 * @param converters
	 */
/*
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		ObjectMapper objectMapper = builder.build();

		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(ValueEnum.class, new JacksonEnumComponent.ValueEnumJsonSerializer());

		objectMapper.registerModule(simpleModule);
		objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
	}
*/


}
