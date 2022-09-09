package com.gbq.docker.uiproject.config.redis;


import com.gbq.docker.uiproject.commons.util.jedis.JedisClientPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

/**
 * @author 郭本琪
 * @description redis的配置
 * @date 2022/9/8 8:46
 * @Copyright 总有一天，会见到成功
 */
@Configuration
public class RedisConfig {
    @Value("${redis.standalone.host}")
    private String STANDALONE_HOST;

    @Value("${redis.standalone.port}")
    private Integer STANDALONE_PORT;


    /**
     *  返回redis的连接
     * @param
     * @since 2022/9/8
     * @return
     */
    @Bean
    public JedisClientPool jedisClientPool(){
        JedisClientPool jedisClientPool = new JedisClientPool();
        jedisClientPool.setJedisPool(jedisPool());
        return jedisClientPool;
    }

    /**
     *  配置reis的连接
     * @param
     * @since 2022/9/8
     * @return
     */
    @Bean
    public JedisPool jedisPool(){
        return new JedisPool(new GenericObjectPoolConfig(),STANDALONE_HOST,STANDALONE_PORT, Protocol.DEFAULT_TIMEOUT,null,Protocol.DEFAULT_DATABASE,null);
    }

}
