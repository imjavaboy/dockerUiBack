package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.domain.dto.SysImageDTO;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 11:44
 * @Copyright 总有一天，会见到成功
 */
public interface SysImageMapper extends BaseMapper<SysImage> {

    /**
     *  获取本地公告镜像
     * @param
     * @since 2022/9/10
     * @return
     */
    List<SysImageDTO> listLocalPublicImage(Page<SysImageDTO> page,@Param("name") String name);

    /**
     *  取本地用户镜像
     * @param
     * @since 2022/9/10
     * @return
     */
    List<SysImageDTO> listLocalUserImage(Page<SysImageDTO> page, @Param("name") String name);

    /**
     * 查询自己的镜像
     * @param
     * @since 2022/9/11
     * @return
     */
    List<SysImage> listSelfImage(String uid, Page<SysImage> page);



}
