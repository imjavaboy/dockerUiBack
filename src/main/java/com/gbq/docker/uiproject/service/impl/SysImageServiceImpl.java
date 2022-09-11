package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.SysImageDTO;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.mapper.SysImageMapper;
import com.gbq.docker.uiproject.service.SysImageService;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.messages.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 11:43
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class SysImageServiceImpl extends ServiceImpl<SysImageMapper, SysImage> implements SysImageService {
    @Resource
    private SysImageMapper imageMapper;
    @Resource
    private SysLoginService loginService;
    @Resource
    private DockerClient dockerClient;

    @Override
    public Page<SysImageDTO> listLocalPublicImage(String name, Page<SysImageDTO> page) {
       List<SysImageDTO> list =  imageMapper.listLocalPublicImage(page,name);


        return page.setRecords(list);
    }

    @Override
    public Page<SysImageDTO> listLocalUserImage(String name, boolean filterOpen, String uid, Page<SysImageDTO> page) {
        List<SysImageDTO> images;
        if(filterOpen) {
            List<SysImage> imageList = imageMapper.selectList(new EntityWrapper<SysImage>()
                    .eq("type", 2)
                    .and().eq("user_id",uid).or().eq("has_open", true));
            images = sysImage2DTO(imageList);

        } else {
            images = imageMapper.listLocalUserImage(page, name);
        }

        return page.setRecords(images);
    }
    private List<SysImageDTO> sysImage2DTO(List<SysImage> list) {
        return list.stream().map(this::sysImage2DTO).collect(Collectors.toList());
    }

    private SysImageDTO sysImage2DTO(SysImage sysImage){
        SysImageDTO dto = new SysImageDTO();
        BeanUtils.copyProperties(sysImage, dto);

        SysLogin login = loginService.getById(sysImage.getId());
        if(login != null) {
            dto.setUsername(login.getUsername());
        }
        return dto;
    }

    @Override
    public ResultVO listHubImage(String name, int limit) {
        if (StringUtils.isBlank(name)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        try {
            List<ImageSearchResult> results = dockerClient.searchImages(name);
            return ResultVOUtils.success(results);
        } catch (DockerRequestException requestException){
            return ResultVOUtils.error(
                    ResultEnum.SERVICE_CREATE_ERROR.getCode(),
                    HttpClientUtils.getErrorMessage(requestException.getMessage()));
        } catch (Exception e) {
            log.error("Docker搜索异常，错误位置：SysImageServiceImpl.listHubImage,出错信息：" + HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.DOCKER_EXCEPTION);
        }
    }
}
