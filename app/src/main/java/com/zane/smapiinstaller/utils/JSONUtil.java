package com.zane.smapiinstaller.utils;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        mapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        mapper.configure(JsonReadFeature.ALLOW_MISSING_VALUES.mappedFeature(), true);
        mapper.configure(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(), true);
    }
    public static String toJson(Object object) throws Exception {
        return mapper.writeValueAsString(object);
    }

    public static void checkJson(String jsonString) throws JsonProcessingException {
        mapper.readValue(jsonString, Object.class);
    }

    public static <T> T fromJson(String jsonString, Class<T> cls) {
        try {
            return mapper.readValue(jsonString, cls);
        } catch (JsonProcessingException e) {
            Log.e("JSON", "Deserialize error", e);
        }
        return null;
    }

    public static <T> T fromJson(String jsonString, TypeReference<T> type) {
        try {
            return mapper.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            Log.e("JSON", "Deserialize error", e);
        }
        return null;
    }
}
