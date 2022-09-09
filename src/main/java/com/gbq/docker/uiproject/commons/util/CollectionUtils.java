package com.gbq.docker.uiproject.commons.util;


import java.util.List;

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

    private static <T> boolean isListEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }
}
