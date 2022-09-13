package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.dto.UserContainerDTO;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.domain.enums.ContainerOpEnum;
import com.gbq.docker.uiproject.domain.enums.ContainerStatusEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:53
 * @Copyright 总有一天，会见到成功
 */
public interface UserContainerService extends IService<UserContainer> {

    /**
     *  获取容器信息
     * @param
     * @since 2022/9/9
     * @return
     */
    UserContainerDTO getById(String objId);

    /**
     * 获取所有容器
     * @param
     * @since 2022/9/9
     * @return
     */
    Page<UserContainerDTO> listContainerByUserId(String uid, String name, Integer status, Page<UserContainer> page);

    /**
     *  创建容器前的校验
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO createContainerCheck(String uid, String imageId, Map<String, String> portMap, String projectId);

    /**
     *  创建容器任务
     * @param uid 用户id
     * @param imageId 镜像id
     * @param cmd 执行命令数组
     * @param portMap 端口map
     * @param containerName 容器名
     * @param projectId 项目名称
     * @param env 环境变量
     * @param destination 内部目录
     * @since 2022/9/12
     * @return
     */
    void createContainerTask(String uid, String imageId, String[] cmd, Map<String, String> portMap,
                             String containerName, String projectId, String[] env,
                             String[] destination, HttpServletRequest request);

    /**
     *  获取容器状态
     * @param
     * @since 2022/9/12
     * @return
     */
    ContainerStatusEnum getStatus(String id);

    /**
     *  同步容器状态
     * @param
     * @since 2022/9/12
     * @return
     */
    Map<String,Integer> syncStatus(String userId);

    /**
     *  修改数据库容器的状态
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO changeStatus(String containerId);

    /**
     *  是否允许容器操作
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO hasAllowOp(String uid, String containerId, ContainerOpEnum start);

    /**
     *  开启容器的任务
     * @param
     * @since 2022/9/12
     * @return
     */
    void startContainerTask(String uid, String containerId);

    Map<String,Integer> sync();

    ResultVO checkPermission(String uid, String id);

    /**
     *  暂停容器
     * @param
     * @since 2022/9/13
     * @return
     */
    void pauseContainerTask(String uid, String containerId);

    /**
     *  重启容器哦
     * @param
     * @since 2022/9/13
     * @return
     */
    void restartContainerTask(String uid, String containerId);

    /**
     *  停止容器
     * @param
     * @since 2022/9/13
     * @return
     */
    void stopContainerTask(String uid, String containerId);

    /**
     *  强制停止容器
     * @param
     * @since 2022/9/13
     * @return
     */
    void killContainerTask(String uid, String containerId);

    /**
     *  从暂停状态恢复
     * @param
     * @since 2022/9/13
     * @return
     */
    void continueContainerTask(String uid, String containerId);
    /**
     *  删除容器
     * @param
     * @since 2022/9/13
     * @return
     */
    void removeContainerTask(String uid, String containerId, HttpServletRequest request);

    /**
     *  或企业容器内部状态
     * @param
     * @since 2022/9/13
     * @return
     */
    ResultVO topContainer(String uid, String containerId);

    /**
     *  修改容器所属项目
     * @param
     * @since 2022/9/13
     * @return
     */
    ResultVO changeBelongProject(String containerId, String projectId, String uid);
}
