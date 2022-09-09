package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.mapper.SysImageMapper;
import com.gbq.docker.uiproject.service.SysImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 11:43
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class SysImageServiceImpl extends ServiceImpl<SysImageMapper, SysImage> implements SysImageService {
}
