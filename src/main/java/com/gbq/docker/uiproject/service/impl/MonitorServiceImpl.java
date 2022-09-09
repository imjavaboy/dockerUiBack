package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.gbq.docker.uiproject.commons.util.*;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.entity.RepositoryImage;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.entity.UserProject;
import com.gbq.docker.uiproject.domain.entity.UserService;
import com.gbq.docker.uiproject.domain.enums.ContainerStatusEnum;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.DockerInfoVO;
import com.gbq.docker.uiproject.domain.vo.DockerNodeInfoVO;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.domain.vo.UserDockerInfoVO;
import com.gbq.docker.uiproject.mapper.RepositoryImageMapper;
import com.gbq.docker.uiproject.mapper.UserContainerMapper;
import com.gbq.docker.uiproject.mapper.UserProjectMapper;
import com.gbq.docker.uiproject.mapper.UserServiceMapper;
import com.gbq.docker.uiproject.service.MonitorService;
import com.gbq.docker.uiproject.service.SysImageService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.swarm.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    @Resource
    private UserContainerMapper userContainerMapper;
    @Resource
    private RepositoryImageMapper repositoryImageMapper;
    @Resource
    private UserProjectMapper userProjectMapper;
    @Resource
    private SysImageService sysImageService;
    @Resource
    private UserServiceMapper userServiceMapper;
    @Resource
    private JedisClient jedisClient;


    @Override
    public ResultVO getUserDockerInfo(String uid) {
        UserDockerInfoVO userDockerInfoVO = new UserDockerInfoVO();
        userDockerInfoVO.setContainerNum(userContainerMapper.countByUserId(uid,null));
        userDockerInfoVO.setContainerRunningNum(userContainerMapper.countByUserId(uid, ContainerStatusEnum.RUNNING.getCode()));
        userDockerInfoVO.setContainerRunningNum(userContainerMapper.countByUserId(uid, ContainerStatusEnum.PAUSE.getCode()));
        userDockerInfoVO.setContainerRunningNum(userContainerMapper.countByUserId(uid, ContainerStatusEnum.STOP.getCode()));
        userDockerInfoVO.setHubImageNum(repositoryImageMapper.selectCount(new EntityWrapper<RepositoryImage>().eq("user_id",uid)));
         userDockerInfoVO.setProjectNum(userProjectMapper.selectCount(new EntityWrapper<UserProject>().eq("user_id", uid)));
         userDockerInfoVO.setUploadImageNum(sysImageService.selectCount(new EntityWrapper<SysImage>().eq("user_id", uid)));
         userDockerInfoVO.setServiceNum(userServiceMapper.selectCount(new EntityWrapper<UserService>().eq("user_id", uid)));


        try {
            String last_login = jedisClient.hget("last_login", uid);
            if (StringUtils.isNotBlank(last_login)) {
                Map<String, String> map = JsonUtils.jsonToMap(last_login);
                Long timestamp = Long.parseLong(map.get("timestamp"));
                String ip = map.get("ip");
                userDockerInfoVO.setLastLogin(new Date(timestamp));
                userDockerInfoVO.setIp(ip);
            }
        } catch (NumberFormatException e) {
            log.error("缓存读取异常，错误位置：{}", "MonitorServiceImpl.getUserDockerInfo()");
        }

        return ResultVOUtils.success(userDockerInfoVO);
    }

    @Override
    public ResultVO getDockerInfo() {

        try {
            Info info = dockerClient.info();
            List<Node> nodes = dockerSwarmClient.listNodes();
            List<com.spotify.docker.client.messages.swarm.Service> serviceList = dockerSwarmClient.listServices();
            DockerInfoVO infoVO = genDockerInfoVO(info);
            infoVO.setNodes(genDockerNodeInfoVO(nodes));
            infoVO.setServiceNum(serviceList.size());

            return ResultVOUtils.success(infoVO);
        } catch (DockerException | InterruptedException e) {
            log.error("读取Docker宿主机信息错误，错误位置：{}，错误栈：{}",
                    "MonitorServiceImpl.getDockerInfo()", HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }


    }

    /**
     *  数据转换，node转换DockerNodeInfoVO
     * @param
     * @since 2022/9/9
     * @return
     */
    private List<DockerNodeInfoVO> genDockerNodeInfoVO(List<Node> nodes) {
        if(nodes == null || nodes.size() == 0) {
            return null;
        }

        List<DockerNodeInfoVO> list = new ArrayList<>();
        for(Node node : nodes) {
            DockerNodeInfoVO nodeInfoVO = new DockerNodeInfoVO();

            nodeInfoVO.setHostName(node.description().hostname());
            nodeInfoVO.setArchitecture(node.description().platform().architecture());
            nodeInfoVO.setDockerVersion(node.description().engine().engineVersion());
            nodeInfoVO.setState(node.status().state());
            nodeInfoVO.setIp(node.status().addr());
            nodeInfoVO.setHasLeader(node.managerStatus() != null);

            list.add(nodeInfoVO);
        }

        return list;
    }

    /**
     *  数据转换 info 转换infoVo
     * @param
     * @since 2022/9/9
     * @return
     */
    private DockerInfoVO genDockerInfoVO(Info info) {
        DockerInfoVO infoVO = new DockerInfoVO();

        infoVO.setHostName(info.name());
        infoVO.setArchitecture(info.architecture());
        infoVO.setOsName(info.operatingSystem());
        infoVO.setCupNum(info.cpus());
        infoVO.setMemorySize(NumberUtils.decimal2Bit((double)(info.memTotal()) / 1024 /1024 / 1024));

        infoVO.setDockerVersion(info.serverVersion());
        infoVO.setImageNum(info.images());
        infoVO.setContainerNum(info.containers());
        infoVO.setContainerRunningNum(info.containersRunning());
        infoVO.setContainerPauseNum(info.containersPaused());
        infoVO.setContainerStopNum(info.containersStopped());

        infoVO.setTime(new Date());

        return infoVO;
    }
}
