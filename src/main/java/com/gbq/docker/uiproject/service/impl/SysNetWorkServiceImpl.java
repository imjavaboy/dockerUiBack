package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.util.CollectionUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.entity.ContainerNetwork;
import com.gbq.docker.uiproject.domain.entity.SysNetwork;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ContainerNetworkVO;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.exception.CustomException;
import com.gbq.docker.uiproject.mapper.ContainerNetworkMapper;
import com.gbq.docker.uiproject.mapper.SysNetworkMapper;
import com.gbq.docker.uiproject.service.SysLogService;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.gbq.docker.uiproject.service.SysNetworkService;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.ContainerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:35
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class SysNetWorkServiceImpl extends ServiceImpl<SysNetworkMapper, SysNetwork> implements SysNetworkService {

    @Resource
    private SysNetworkMapper networkMapper;
    @Resource
    private ContainerNetworkMapper containerNetworkMapper;
    @Resource
    private DockerClient dockerClient;
    @Resource
    private SysLoginService loginService;
    @Resource
    private SysLogService sysLogService;


    @Override
    public SysNetwork getById(String id) {
        return networkMapper.selectById(id);
    }

    @Override
    public Object listAllNetwork(Page<SysNetwork> page, Boolean hasPublic) {
        List<SysNetwork> list = networkMapper.listAllNetwork(page, hasPublic);

        return page.setRecords(list);
    }

    @Override
    public Page<SysNetwork> listSelfNetwork(Page<SysNetwork> page, String uid) {
        List<SysNetwork> list = networkMapper.listSelfNetwork(page, uid);

        return page.setRecords(list);
    }

    @Override
    public Page<SysNetwork> listSelfAndPublicNetwork(Page<SysNetwork> page, String uid) {
        List<SysNetwork> list = networkMapper.listSelfAndPublicNetwork(page, uid);

        return page.setRecords(list);
    }

    @Override
    @Transactional(rollbackFor = CustomException.class)
    public ResultVO syncByContainerId(String containerId) {
        try {
            List<ContainerNetwork> dbList = containerNetworkMapper.selectList(new EntityWrapper<ContainerNetwork>().eq("container_id", containerId));
            boolean[] dbFlag = new boolean[dbList.size()];
            Arrays.fill(dbFlag, false);

            ContainerInfo containerInfo =  dockerClient.inspectContainer(containerId);
            ImmutableMap<String, AttachedNetwork> networkImmutableMap =  containerInfo.networkSettings().networks();

            int addCount = 0, deleteCount = 0;
            if(networkImmutableMap != null && networkImmutableMap.size() > 0) {
                boolean flag = false;
                for(AttachedNetwork attachedNetwork : networkImmutableMap.values()) {
                    String networkId = attachedNetwork.networkId();
                    if(StringUtils.isNotBlank(networkId)) {
                        // 判断数据库中是否有该条记录
                        for(int i=0; i<dbList.size(); i++) {
                            if(dbFlag[i]) {
                                continue;
                            }
                            if(hasExist(containerId, networkId)) {
                                dbFlag[i] = true;
                                flag = true;
                                break;
                            }
                        }

                        // 保存新纪录
                        if(!flag) {
                            ContainerNetwork containerNetwork = new ContainerNetwork(containerId, networkId);
                            containerNetworkMapper.insert(containerNetwork);
                            addCount++;
                        }
                    }
                }

                // 删除失效记录
                for(int i=0; i< dbList.size(); i++) {
                    if(!dbFlag[i]) {
                        containerNetworkMapper.deleteById(dbList.get(i).getId());
                        deleteCount++;
                    }
                }
            }

            Map<String, Integer> map = new HashMap<>(16);
            map.put("add", addCount);
            map.put("delete", deleteCount);
            return ResultVOUtils.success(map);
        } catch (Exception e) {
            return ResultVOUtils.error(ResultEnum.CONTAINER_NETWORK_SYNC_ERROR);
        }

    }

    private boolean hasExist(String containerId, String networkId) {
        List<ContainerNetwork> list = containerNetworkMapper.selectList(new EntityWrapper<ContainerNetwork>()
                .eq("container_id", containerId)
                .eq("network_id", networkId));

        return CollectionUtils.isListNotEmpty(list);
    }

    @Override
    public ResultVO listByContainerId(String containerId) {
        List<ContainerNetwork> list = containerNetworkMapper.selectList(
                new EntityWrapper<ContainerNetwork>().eq("container_id", containerId));
        List<ContainerNetworkVO> res = new ArrayList<>();

        for(ContainerNetwork containerNetwork : list) {
            ContainerNetworkVO vo = new ContainerNetworkVO();
            BeanUtils.copyProperties(containerNetwork, vo);
            // 设置网络
            vo.setNetwork(networkMapper.selectById(containerNetwork.getNetworkId()));
            res.add(vo);
        }

        return ResultVOUtils.success(res);
    }

    @Override
    public ResultVO hasPermission(String networkId, String userId) {
        SysNetwork network = getById(networkId);
        if(network == null) {
            return ResultVOUtils.error(ResultEnum.NETWORK_NOT_EXIST);
        }

        // 公共网络均能访问
        if(network.getHasPublic()) {
            return ResultVOUtils.success();
        }

        String roleName = loginService.getRoleName(userId);

        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            // 普通用户无法访问他人网络
            if(!userId.equals(network.getUserId())) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }
        return ResultVOUtils.success();
    }
}
