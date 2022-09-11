package com.gbq.docker.uiproject.commons.component;


import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.UserContainerDTO;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.domain.enums.ContainerStatusEnum;
import com.gbq.docker.uiproject.service.ProjectService;
import com.gbq.docker.uiproject.service.SysLoginService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:59
 * @Copyright 总有一天，会见到成功
 */
@Component
public class UserContainerDTOConvert {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SysLoginService sysLoginService;
    @Value("${docker.server.address}")
    private String serverIp;

    public UserContainerDTO convert(UserContainer container) {
        if(container == null) {
            return null;
        }
        UserContainerDTO dto = new UserContainerDTO();
        BeanUtils.copyProperties(container, dto);

        String projectId = container.getProjectId();
        if(StringUtils.isNotBlank(projectId)) {
            String projectName = projectService.getProjectName(projectId);
            dto.setProjectName(projectName);
        }

        Integer status = container.getStatus();
        if(status != null) {
            dto.setStatusName(ContainerStatusEnum.getMessage(status));
        }

        String userId = projectService.getUserId(container.getProjectId());
        if(StringUtils.isNotBlank(projectId)) {
            String username= sysLoginService.getById(userId).getUsername();
            dto.setUsername(username);
        }

        if(StringUtils.isNotBlank(projectId)) {
            String projectName = projectService.getProjectName(projectId);
            dto.setProjectName(projectName);
        }

        dto.setIp(serverIp);

        return dto;
    }

    public List<UserContainerDTO> convert(List<UserContainer> containers) {
        return containers.stream().map(this::convert).collect(Collectors.toList());
    }

    public Page<UserContainerDTO> convert(Page<UserContainer> page) {
        List<UserContainer> containers = page.getRecords();
        List<UserContainerDTO> containerDTOS = convert(containers);

        Page<UserContainerDTO> page1 = new Page<>();
        BeanUtils.copyProperties(page, page1);
        page1.setRecords(containerDTOS);

        return page1;
    }
}
