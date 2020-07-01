package com.aatroxc.wecommunity.config;

import com.aatroxc.wecommunity.model.enums.ValueEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

/**
 * Jackson序列化枚举时，默认序列化 name， 这个序列化器将其序列化成自定义的 value
 * 指定 @JsonComponent 可以自动注册到 Jackson
 *
 * 经测试，如果项目正在运行，然后改变了代码，idea自动加载后、或ctrl+F9 手动加载后，
 * 此序列化器就不起作用了，必需要项目重启才行.
 * 或者在 WebMvcConfig 中手动注册到 Jackson?
 *
 * @author mafei007
 * @date 2020/4/20 22:26
 */

@JsonComponent
public class JacksonEnumComponent {

	public static class ValueEnumJsonSerializer extends JsonSerializer<ValueEnum> {

		@Override
		public void serialize(ValueEnum valueEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			if (null == valueEnum) {
				return;
			}
			jsonGenerator.writeObject(valueEnum.getValue());
		}

	}

/*
	public static class ValueEnumJsonDeserializer extends JsonDeserializer<ValueEnum> {
		@Override
		public ValueEnum deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		}
	}
*/

}
