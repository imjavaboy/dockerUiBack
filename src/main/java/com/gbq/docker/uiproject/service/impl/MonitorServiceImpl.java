package com.gbq.docker.uiproject.service.impl;


import com.gbq.docker.uiproject.config.docker.DockerSwarmConfig;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.MonitorService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.swarm.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 9:07
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class MonitorServiceImpl implements MonitorService {
    @Resource
    private DockerClient dockerClient;
    @Resource
    private DockerClient dockerSwarmClient;

    @Override
    public ResultVO getUserDockerInfo(String uid) {
        return null;
    }

    @Override
    public ResultVO getDockerInfo() {
//
//        Info info = dockerClient.info();
//        List<Node> nodes = dockerSwarmClient.listNodes();
//        List<com.spotify.docker.client.messages.swarm.Service> serviceList = dockerSwarmClient.listServices();
//


        return null;
    }
}
