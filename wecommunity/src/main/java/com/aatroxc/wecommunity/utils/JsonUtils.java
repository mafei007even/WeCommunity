package com.aatroxc.wecommunity.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @author mafei
 * @Date 2020/3/6 23:27
 */
public class JsonUtils {

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * 将对象转换成json字符串。
     * @param data
     * @return
     */
    public static String objectToJson(Object data) {
        if (data == null) {
            return null;
        }
        if (data.getClass() == String.class) {
            return (String) data;
        }
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + data, e);
        }
        return null;
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param clazz    对象中的object类型
     * @return
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
            logger.error("json解析出错：" + jsonData);
        }
        return null;
    }

    /**
     * 将json数据转换成pojo对象list
     * <p>Title: jsonToList</p>
     * <p>Description: </p>
     *
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = MAPPER.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            logger.error("json解析出错：" + jsonData, e);
        }

        return null;
    }

    /**
     * 2020年3月6日
     * 封装用于转成 Map<String, Object>，其实kv都是字符串.
     *
     * @param jsonData
     * @param keyClass
     * @param valueClass
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> jsonToMap(String jsonData, Class<K> keyClass, Class<V> valueClass) {
        MapType mapType = MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        try {
            // 这样也行，更简单
            // Map<K, V> map = MAPPER.readValue(jsonData, new TypeReference<Map<K, V>>() {
            // });
            /**
             *  这样可以将 value 转为 List，但如果提供JsonUtils的静态方法的话，方法参数太麻烦
             *  Map<String, List<Object>> map = MAPPER.readValue(jsonData, new TypeReference<Map<String, List<Object>>>() {
             *  });
             */
            Map<K, V> map = MAPPER.readValue(jsonData, mapType);
            return map;
        } catch (JsonProcessingException e) {
            logger.error("json解析出错：" + jsonData, e);
        }

        return null;
    }


    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    /*
    测试
     */
    public static void main(String[] args) {

        String json = "{\"1\":\"魅族（MEIZU）\",\"2\":\"魅族PRO 6s\",\"3\":2016,\"5\":163,\"6\":\"其它\",\"7\":\"Android\",\"8\":\"联发科（MTK）\",\"9\":\"X25（MT6797T）\",\"10\":\"十核\",\"11\":2.5,\"14\":5.2,\"15\":\"1920*1080\",\"16\":500,\"17\":1200,\"18\":3060}";

        Map<String, Object> map = JsonUtils.jsonToMap(json, String.class, Object.class);
        System.out.println(map);

    }

}
