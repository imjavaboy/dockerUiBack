package com.gbq.docker.uiproject.commons.util.jedis;


import java.util.List;
import java.util.Set;

/**
 * @author 郭本琪
 * @description jedis的客户端接口
 * @date 2022/9/8 8:37
 * @Copyright 总有一天，会见到成功
 */

public interface JedisClient {
    /**
     *  设置key，value
     * @param
     * @since 2022/9/8
     * @return
     */
    String set(String key,String value);
    /**
     *  根据key获取valuye
     * @param
     * @since 2022/9/8
     * @return
     */
    String get(String key);

    /**
     *  判断key是否存在
     * @param
     * @since 2022/9/8
     * @return
     */
    Boolean exists(String key);

    /**
     *  删除key
     * @param key
     * @since 2022/9/8
     * @return
     */
    Long del(String key);

    /**
     *  设置过期时间
     * @param
     * @since 2022/9/8
     * @return
     */
    Long expire(String key, int seconds);

    /**
     *  获取过期时间
     * @param
     * @since 2022/9/8
     * @return
     */
    Long ttl(String key);
    /**
     * 自加
     */
    Long incr(String key);

    /**
     * 设置hash类型
     */
    Long hset(String key, String field, String value);

    /**
     * 获取hash类型
     */
    String hget(String key, String field);

    /**
     * 删除hash类型中field
     */
    Long hdel(String key, String... field);

    /**
     * 指定hash中某个field是否存在
     */
    Boolean hexists(String key, String field);

    /**
     * 返回hash的field的List
     */
    Set<String> hkeys(String key);

    /**
     * 返回hash的value的List
     */
    List<String> hvals(String key);

    /**
     * 向set中添加成员，如果成员已存在，不再添加
     */
    Long sadd(String key, String... members);

    /**
     * 获取set所有成员
     */
    Set<String> smembers(String key);

    /**
     * 判断指定成员是否存在于set中
     */
    Boolean sismember(String key, String member);

    /**
     * 向set中删除成员
     */
    Long srem(String key, String... members);

    /**
     * 求set中成员数量
     */
    Long scard(String key);

    /**
     * 随即返回一个set成员
     */
    String srandmember(String key);

    /**
     * 添加成员。如果成员存在，会用新的score替代原有的score，返回值是新加入到集合中的成员个数
     */
    Long zadd(String key, Double score, String member);

    /**
     * 获取集合中下标从start到end的成员
     */
    Set<String> zrange(String key, long start, long end);

    /**
     * 返回score在[min,max]的成员并按照score排序。
     */
    Set<String> zrangeByScore(String key, double min, double max);

    /**
     * 删除成员
     */
    Long zrem(String key, String... members);

    /**
     * 按照下标范围删除成员
     */
    Long zremrangeByRank(String key, long start, long end);

    /**
     * 按照score范围删除成员
     */
    Long zremrangeByScore(String key, double min, double max);
}
