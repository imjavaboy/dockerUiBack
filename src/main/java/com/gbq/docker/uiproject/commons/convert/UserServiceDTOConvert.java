package com.gbq.docker.uiproject.commons.convert;


import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.UserServiceDTO;
import com.gbq.docker.uiproject.domain.entity.UserService;
import com.gbq.docker.uiproject.service.ProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:49
 * @Copyright 总有一天，会见到成功
 */
@Component
public class UserServiceDTOConvert {


    @Resource
    private ProjectService projectService;

    @Value("${docker.swarm.manager.address}")
    private String serverIp;

    public UserServiceDTO convert(UserService userService) {
        if(userService == null) {
            return null;
        }

        UserServiceDTO dto = new UserServiceDTO();
        BeanUtils.copyProperties(userService, dto);

        String projectId = userService.getProjectId();
        if(StringUtils.isNotBlank(projectId)) {
            String projectName = projectService.getProjectName(projectId);
            dto.setProjectName(projectName);
        }

        dto.setIp(serverIp);

        return dto;
    }

    public List<UserServiceDTO> convert(List<UserService> userServices) {
        return userServices.stream().map(this::convert).collect(Collectors.toList());
    }
}
