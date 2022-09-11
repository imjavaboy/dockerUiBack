package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.dto.UserServiceDTO;
import com.gbq.docker.uiproject.domain.entity.UserService;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:43
 * @Copyright 总有一天，会见到成功
 */
public interface UserServiceService  extends IService<UserService> {

    /**
     *  获取用户服务信息
     * @param
     * @since 2022/9/9
     * @return
     */
    UserService getById(String objId);

    /**
     *  获取所有服务列表
     * @param
     * @since 2022/9/10
     * @return
     */
    Page<UserServiceDTO> listUserServiceByUserId(String uid, Page<UserService> page);
}
