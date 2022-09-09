package com.gbq.docker.uiproject.config.docker;


import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 10:25
 * @Copyright 总有一天，会见到成功
 */
@Configuration
public class DockerSwarmConfig {
    @Value("${docker.swarm.manager.url}")
    private String serverUrl;

    @Bean(name = "dockerSwarmClient")
    DockerClient dockerClient() {
        return DefaultDockerClient.builder()
                .uri(URI.create(serverUrl))
                .build();
    }
}
