package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.component.UserContainerDTOConvert;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.UserContainerDTO;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.ProjectService;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.gbq.docker.uiproject.service.UserContainerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description 容器模块
 * @date 2022/9/9 22:43
 * @Copyright 总有一天，会见到成功
 */
@Api(tags = "容器模块")
@Slf4j
@RestController
@RequestMapping("/container")
public class ContainerController {


    @Resource
    private SysLoginService loginService;
    @Resource
    private UserContainerService containerService;
    @Resource
    private ProjectService projectService;
    @Resource
    private UserContainerDTOConvert dtoConvert;

    @ApiOperation("获取所有容器")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listContainer(@RequestAttribute String uid, String name, Integer status,
                                  @RequestParam(defaultValue = "1") Integer current,
                                  @RequestParam(defaultValue = "10") Integer size) {
        // 鉴权
        String roleName = loginService.getRoleName(uid);
        // 角色无效
        if (StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }

        Page<UserContainer> page = new Page<>(current, size, "update_date", false);
        Page<UserContainerDTO> selectPage = null;

        if (RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            selectPage = containerService.listContainerByUserId(uid, name, status, page);
        } else if (RoleEnum.ROLE_SYSTEM.getMessage().equals(roleName)) {
            selectPage = containerService.listContainerByUserId(null, name, status, page);
        }

        return ResultVOUtils.success(selectPage);
    }

    @ApiOperation("获取容器内所有的项目，普通用户获取本人项目的容器，系统管理员任意项目的容器")
    @GetMapping("/project/{projectId}/list")
    public ResultVO listContainerByProject(@RequestAttribute String uid, @PathVariable String projectId, Page<UserContainer> page) {
        String roleName = loginService.getRoleName(uid);

        if (StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }
        if (RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            if (!projectService.hasBelong(projectId, uid)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }

        Page<UserContainer> pageList = containerService.selectPage(page, new EntityWrapper<UserContainer>().eq("project_id", projectId));
        return ResultVOUtils.success(dtoConvert.convert(pageList));

    }


}
