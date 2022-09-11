package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.domain.entity.ProjectLog;
import com.gbq.docker.uiproject.domain.enums.ProjectLogTypeEnum;
import com.gbq.docker.uiproject.mapper.ProjectLogMapper;
import com.gbq.docker.uiproject.service.ProjectLogService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/10 10:15
 * @Copyright 总有一天，会见到成功
 */
@Service
public class ProjectLogServiceImpl extends ServiceImpl<ProjectLogMapper, ProjectLog> implements ProjectLogService {
    @Resource
    private ProjectLogMapper logMapper;


    @Override
    public void saveSuccessLog(String projectId, String objId, ProjectLogTypeEnum projectLogTypeEnum) {

        ProjectLog log = new ProjectLog(projectId, objId, projectLogTypeEnum.getCode(), projectLogTypeEnum.getMessage());
        logMapper.insert(log);

    }
}
