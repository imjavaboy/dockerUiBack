package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.entity.SysNetwork;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:35
 * @Copyright 总有一天，会见到成功
 */
public interface SysNetworkService extends IService<SysNetwork> {

    SysNetwork getById(String id);

    /**
     *  获取所有网络
     * @param
     * @since 2022/9/11
     * @return
     */
    Object listAllNetwork(Page<SysNetwork> page, Boolean hasPublic);

    /**
     *  获取个人网阔
     * @param
     * @since 2022/9/11
     * @return
     */
    Page<SysNetwork> listSelfNetwork(Page<SysNetwork> page, String uid);

    /**
     *  个人和公告网络
     * @param
     * @since 2022/9/11
     * @return
     */
    Page<SysNetwork> listSelfAndPublicNetwork(Page<SysNetwork> page, String uid);

    /**
     *  同步容器的网luo
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO syncByContainerId(String containerId);

    /**
     *  获取容器所有网络
     * @param
     * @since 2022/9/13
     * @return
     */
    ResultVO listByContainerId(String id);

    /**
     *  是否有权限访问
     * @param
     * @since 2022/9/13
     * @return
     */
    ResultVO hasPermission(String id, String uid);
}
