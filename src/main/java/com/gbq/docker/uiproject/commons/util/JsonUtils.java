package com.gbq.docker.uiproject.commons.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.exception.JsonException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 郭本琪
 * @description JSON工具类
 * @date 2022/9/8 8:56
 * @Copyright 总有一天，会见到成功
 */

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();



    /**
     *  json转对象
     * @param
     * @since 2022/9/8
     * @return
     */
    public static <T> T jsonToObject(String jsonData,Class<T> beanType) throws JsonException {
        try {
            return MAPPER.readValue(jsonData,beanType);
        } catch (IOException e) {
           throw new JsonException("json转换异常");
        }

    }

    /**
     * 对象转字符串
     * @param
     * @since 2022/9/8
     * @return
     */
    public static String objectToJson(Object listFirst) {
        try {
            return MAPPER.writeValueAsString(listFirst);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String mapToJson(Map data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <T> Map<String, T> jsonToMap(String jsonData) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonData, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  Json字符串--> List<对象>
     * @param 
     * @since 2022/9/13
     * @return 
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            return MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
