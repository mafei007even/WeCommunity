package com.aatroxc.wecommunity.factory;

import com.google.common.collect.Maps;
import com.aatroxc.wecommunity.model.enums.ValueEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mafei007
 * @date 2020/4/17 17:31
 */


public class StringToEnumConverterFactory implements ConverterFactory<String, ValueEnum<?>> {

	private static final Map<Class, Converter> CONVERTERS = Maps.newHashMap();

	/**
	 * 获取从 String 转换为 ValueEnum 的转换器
	 * 
	 * @param targetType 转换后的类型
	 * @param <T> 泛型，有多个实现
	 * @return 返回一个转换器
	 */
	@Override
	public <T extends ValueEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
		Converter converter = CONVERTERS.get(targetType);
		if (converter == null) {
			converter = new StringToEnumConverter<>(targetType);
			CONVERTERS.put(targetType, converter);
		}
		return converter;
	}





	private static class StringToEnumConverter<E extends ValueEnum<?>>
			implements Converter<String, E> {

		private Map<String, E> enumMap = new HashMap<>();

		private Class<E> enumType;

		private StringToEnumConverter(Class<E> enumType) {
			Assert.notNull(enumType, "Enum type argument cannot be null");

			this.enumType = enumType;
			E[] enums = enumType.getEnumConstants();
			for (E e : enums) {
				enumMap.put(e.getValue().toString(), e);
			}
		}

		@Override
		public E convert(String s) {
			E valueEnum = enumMap.get(s);
			Assert.notNull(valueEnum, "Cannot convert " + s + " to " + this.enumType.getSimpleName() + " by custom value.");
			return valueEnum;
		}
	}

}
