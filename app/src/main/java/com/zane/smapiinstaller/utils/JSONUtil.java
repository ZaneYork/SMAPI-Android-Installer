package com.zane.smapiinstaller.utils;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON工具类
 * @author Zane
 */
public class JSONUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        // 允许未定义的属性
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        // 允许尾部额外的逗号
        MAPPER.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        // 允许数组设置空值
        MAPPER.configure(JsonReadFeature.ALLOW_MISSING_VALUES.mappedFeature(), true);
        // 允许Java注释
        MAPPER.configure(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(), true);
    }

    /**
     * 转JSON
     * @param object 数据
     * @return JSON字符串
     * @throws Exception 异常
     */
    public static String toJson(Object object) throws Exception {
        return MAPPER.writeValueAsString(object);
    }

    /**
     * 校验JSON
     * @param jsonString JSON字符串
     * @throws JsonProcessingException 异常
     */
    public static void checkJson(String jsonString) throws JsonProcessingException {
        MAPPER.readValue(jsonString, Object.class);
    }

    /**
     * JSON反序列化
     * @param jsonString JSON
     * @param cls        目标类型
     * @param <T>        泛型参数
     * @return 数据
     */
    public static <T> T fromJson(String jsonString, Class<T> cls) {
        try {
            return MAPPER.readValue(jsonString, cls);
        } catch (JsonProcessingException e) {
            Log.e("JSON", "Deserialize error", e);
        }
        return null;
    }

    /**
     * JSON反序列化
     * @param jsonString JSON
     * @param type       目标类型
     * @param <T>        泛型参数
     * @return 数据
     */
    public static <T> T fromJson(String jsonString, TypeReference<T> type) {
        try {
            return MAPPER.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            Log.e("JSON", "Deserialize error", e);
        }
        return null;
    }
}
