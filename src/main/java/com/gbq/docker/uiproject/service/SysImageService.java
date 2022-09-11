package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.dto.SysImageDTO;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

/**
 * @author 郭本琪
 * @description 镜像服务
 * @date 2022/9/9 11:42
 * @Copyright 总有一天，会见到成功
 */
public interface SysImageService extends IService<SysImage> {

    /**
     *  获取本地公共镜像
     * @param
     * @since 2022/9/10
     * @return
     */
    Page<SysImageDTO> listLocalPublicImage(String name, Page<SysImageDTO> page);

    /**
     *  获取本地用户镜像
     * @param
     * @since 2022/9/10
     * @return
     */
    Page<SysImageDTO> listLocalUserImage(String name, boolean filterOpen, String uid, Page<SysImageDTO> page);

    /**
     *  查找hub镜像
     * @param
     * @since 2022/9/10
     * @return
     */
    ResultVO listHubImage(String name, int limit);
}
