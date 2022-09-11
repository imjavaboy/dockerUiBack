package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.dto.UserContainerDTO;
import com.gbq.docker.uiproject.domain.entity.UserContainer;

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
}
