package com.gbq.docker.uiproject.commons.util;


import java.util.Random;
import java.util.UUID;

/**
 * @author 郭本琪
 * @description 随机工具类
 * @date 2022/9/11 21:34
 * @Copyright 总有一天，会见到成功
 */

public final class RandomUtils {
    private static final String ALLCHAR
            = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LETTERCHAR
            = "abcdefghijkllmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERCHAR = "0123456789";


    /**
     *  生成指定范围的随机数
     * @param
     * @since 2022/9/11
     * @return
     */
    public static int integer(int scopeMin, int scoeMax) {
        Random random = new Random();
        return (random.nextInt(scoeMax) % (scoeMax - scopeMin + 1) + scopeMin);
    }

    /**
     *  返回固定长度的随机数字
     * @param
     * @since 2022/9/11
     * @return
     */
    public static String number(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBERCHAR.charAt(random.nextInt(NUMBERCHAR.length())));
        }
        return sb.toString();
    }

    /**
     *  返回固定长度的（包含字母 和数字）
     * @param
     * @since 2022/9/11
     * @return
     */
    public static String stringWithNumber(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString();
    }
    /**
     *  只包含字母的固定长度字符传
     * @param
     * @since 2022/9/11
     * @return
     */
    public static String string(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(LETTERCHAR.length())));
        }
        return sb.toString();
    }
    /**
     *  数字转字符串，不够长则补零
     * @param
     * @since 2022/9/11
     * @return
     */
    public static String num2String(long num, int length) {
        StringBuffer sb = new StringBuffer();
        String strNum = String.valueOf(num);
        if (length - strNum.length() >= 0) {
            sb.append(zeroString(length - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + length + "的字符串发生异常！");
        }
        sb.append(strNum);
        return sb.toString();
    }

    /**
     *  生成纯零的字符串
     * @param
     * @since 2022/9/11
     * @return
     */
    public static String zeroString(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     *  随机返回数组种的某个元素
     * @param
     * @since 2022/9/11
     * @return
     */
    public static <T> T randomItem(T[] params){
        int index = integer(0, params.length);
        return params[index];
    }

    /**
     *  生成32未的uuid
     * @param
     * @since 2022/9/11
     * @return
     */
    public static String uuid(){
        //36未
        UUID uuid = UUID.randomUUID();
        String s = uuid.toString();
        return s.substring(0, 8) + s.substring(9, 13) +
                s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
    }
    /**
     *  根据时间乘胜随机数15位
     * @param
     * @since 2022/9/11
     * @return
     */
    public static String time() {
        long millis = System.currentTimeMillis();
        Random random = new Random();
        int end2 = random.nextInt(99);
        return millis + String.format("%02d", end2);
    }


}
