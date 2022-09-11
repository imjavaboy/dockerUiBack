package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.domain.entity.UserService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 郭本琪
 * @description 用户服务表
 * @date 2022/9/9 11:50
 * @Copyright 总有一天，会见到成功
 */
public interface UserServiceMapper extends BaseMapper<UserService> {
    /**
     *  获取所有服务
     * @param
     * @since 2022/9/10
     * @return
     */
    List<UserService> listServiceByUserId(Page<UserService> page,@Param("uid") String uid);
}
