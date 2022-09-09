package com.gbq.docker.uiproject.config.mybatisplus;


import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author 郭本琪
 * @description mybtisplus配置
 * @date 2022/9/9 12:17
 * @Copyright 总有一天，会见到成功
 */
@EnableTransactionManagement
@Configuration
@MapperScan("com.gbq.docker.uiproject.mapper*")
public class MyBatisPlusConfig {
    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
