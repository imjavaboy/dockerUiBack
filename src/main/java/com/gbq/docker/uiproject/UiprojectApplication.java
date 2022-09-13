package com.gbq.docker.uiproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = "com.gbq.docker.uiproject.mapper")
public class UiprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(UiprojectApplication.class, args);
    }

}
