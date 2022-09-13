package com.gbq.docker.uiproject.commons.util;


import java.util.*;

/**
 * @author 郭本琪
 * @description 集合工具类
 * @date 2022/9/8 11:20
 * @Copyright 总有一天，会见到成功
 */

public class CollectionUtils {


    public static <T> T getListFirst(List<T> list){
        return isListEmpty(list) ? null : list.get(0);
    }

    public static <T> boolean isListEmpty(List<T> list) {

        return list == null || list.size() == 0;
    }
    /**
     *  判空
     * @param
     * @since 2022/9/12
     * @return
     */
    public static boolean isArrayEmpty(String[] strings) {
        if(strings == null || strings.length == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static Map<String, String> mapJson2map(String json) {
        Map<String, String> labels = null;
        if(StringUtils.isNotBlank(json)) {
            labels = JsonUtils.jsonToMap(json);
            // 解决前台发送空map问题
            CollectionUtils.removeNullEntry(labels);
        }
        return labels;
    }

    /**
     *  移除map种的空值
     * @param
     * @since 2022/9/12
     * @return
     */
    private static void removeNullEntry(Map<String, String> map) {
        removeNullKey(map);
        removeNullValue(map);
    }
    public static void removeNullKey(Map map){
        Set set = map.keySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            Object obj = (Object) iterator.next();
            remove(obj, iterator);
        }
    }
    public static void removeNullValue(Map map){
        Set set = map.keySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            Object obj = (Object) iterator.next();
            Object value =(Object)map.get(obj);
            remove(value, iterator);
        }
    }
    private static void remove(Object obj,Iterator iterator){
        if(obj instanceof String){
            String str = (String)obj;
            if(StringUtils.isEmpty(str)){
                iterator.remove();
            }
        }else if(obj instanceof Collection){
            Collection col = (Collection)obj;
            if(col==null||col.isEmpty()){
                iterator.remove();
            }

        }else if(obj instanceof Map){
            Map temp = (Map)obj;
            if(temp==null||temp.isEmpty()){
                iterator.remove();
            }

        }else if(obj instanceof Object[]){
            Object[] array =(Object[])obj;
            if(array==null||array.length<=0){
                iterator.remove();
            }
        }else{
            if(obj==null){
                iterator.remove();
            }
        }
    }

    /**
     *  字符串转字符数组
     * @param
     * @since 2022/9/12
     * @return
     */
    public static String[] str2Array(String str, String split) {
        if(StringUtils.isBlank(str)) {
            return null;
        }

        return str.split(split);
    }


    public static <T> boolean isListNotEmpty(List<T> list) {
        return !isListEmpty(list);
    }

    public static boolean isNotArrayEmpty(String[] strings) {
        return !isArrayEmpty(strings);
    }
}
