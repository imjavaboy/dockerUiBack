package com.gbq.docker.uiproject.commons.util;


/**
 * @author 郭本琪
 * @description 数值工具类
 * @date 2022/9/9 10:43
 * @Copyright 总有一天，会见到成功
 */

public class NumberUtils extends org.apache.commons.lang3.math.NumberUtils {
    /**
     *  保留两位
     * @param
     * @since 2022/9/9
     * @return
     */
    public static  double decimal2Bit(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    /**
     *  保留三位
     * @param
     * @since 2022/9/9
     * @return
     */
    public static  double decimal3Bit(double d) {
        return (double) Math.round(d * 1000) / 1000;
    }
}
