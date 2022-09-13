package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.dto.SysImageDTO;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

import javax.servlet.http.HttpServletRequest;

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

    /**
     *  查询自己的镜像
     * @param
     * @since 2022/9/11
     * @return
     */
    Page<SysImage>selfImage(String uid, Page<SysImage> page);

    /**
     *  拉取镜像之前的校验
     * @param
     * @since 2022/9/11
     * @return
     */
    ResultVO pullImageCheck(String imageName, String uid);

    /**
     *  根据名字获取镜像
     * @param
     * @since 2022/9/11
     * @return
     */
    SysImage getByFullName(String fullName);


    /**
     *  同步本地镜像和数据库镜像
     * @param
     * @since 2022/9/11
     * @return
     */
    ResultVO sync();

    /**
     *  从dockerHub拉取镜像
     * @param
     * @since 2022/9/11
     * @return
     */
    void pullImageTask(String imageName, String uid, HttpServletRequest request);


    /**
     *  某人是否有权限查看此镜像
     * @param
     * @since 2022/9/11
     * @return
     */
    Boolean hasAuthImage(String userId, SysImage image);

    /**
     *  根据id获取镜像
     * @param
     * @since 2022/9/11
     * @return
     */
    SysImage getById(String id);
    /**
     *  查询镜像所暴露的端口
     * @param
     * @since 2022/9/11
     * @return
     */
    ResultVO listExportPort(String id, String uid);

    /**
     *  获取镜像详情
     * @param
     * @since 2022/9/13
     * @return
     */
    ResultVO inspectImage(String id, String uid);

    /**
     *  查看镜像历史
     * @param 
     * @since 2022/9/13
     * @return 
     */
    ResultVO getHistory(String id, String uid);

    /**
     *  导出镜像
     * @param 
     * @since 2022/9/13
     * @return 
     */
    ResultVO exportImage(String id, String uid);
}
