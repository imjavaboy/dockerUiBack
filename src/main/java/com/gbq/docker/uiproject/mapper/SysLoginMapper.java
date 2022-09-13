package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author 郭本琪
 * @description
 * @date 2022/9/7 15:42
 * @Copyright 总有一天，会见到成功
 */

public interface SysLoginMapper extends BaseMapper<SysLogin> {

    /**
     *  获取所有登录用户id
     * @param
     * @since 2022/9/11
     * @return
     */
    List<String> listId();

    /**
     *  判断id是否还存在
     * @param
     * @since 2022/9/11
     * @return
     */
    boolean hasExist(String id);
}
