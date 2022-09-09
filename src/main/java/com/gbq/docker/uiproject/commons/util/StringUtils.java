package com.gbq.docker.uiproject.commons.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 郭本琪
 * @description 字符串工具类
 * @date 2022/9/8 8:12
 * @Copyright 总有一天，会见到成功
 */

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    /**
     *  判断参数是否为空
     * @since 2022/9/8
     * @return
     */
    public static boolean isBlank(String... args) {
        for(String s : args) {
            if(org.apache.commons.lang3.StringUtils.isBlank(s)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isNotBlank(String... args) {
        return !isBlank(args);
    }

    public static boolean isNumeric(String...args) {
        for(String s : args) {
            if(org.apache.commons.lang3.StringUtils.isNumeric(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotNumeric(String... args) {
        return !isNumeric(args);
    }

    /**
     *  检查是字母数字
     * @param
     * @since 2022/9/8
     * @return
     */
    public static  boolean isAlphaOrNumeric(String s) {
        String regex = "^[a-z0-9A-Z]+$";
        return s.matches(regex);
    }

    public static  boolean isNotAlphaOrNumeric(String s) {
        return !isAlphaOrNumeric(s);
    }

    /**
     *  检查是否是email
     * @param
     * @since 2022/9/8
     * @return
     */
    public static boolean isEmail(String string) {
        if (string == null) {
            return false;
        }
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches()){
            return true;
        }else {
            return  false;
        }

    }
}
