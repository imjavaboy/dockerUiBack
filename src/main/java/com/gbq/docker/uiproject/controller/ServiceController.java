package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.convert.UserServiceDTOConvert;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.UserServiceDTO;
import com.gbq.docker.uiproject.domain.entity.UserService;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.ProjectService;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.gbq.docker.uiproject.service.UserServiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/10 19:28
 * @Copyright 总有一天，会见到成功
 */
@RestController
@Slf4j
@RequestMapping("/service")
@Api(tags = "服务模块")
public class ServiceController {

    @Resource
    private SysLoginService loginService;
    @Resource
    private UserServiceService userServiceService;
    @Resource
    private ProjectService projectService;
    @Resource
    private UserServiceDTOConvert dtoConvert;

    @GetMapping("/list")
    @ApiOperation("获取服务列表")
    public ResultVO listService(@RequestAttribute String uid, Page<UserService> page) {
        String roleName = loginService.getRoleName(uid);

        if(StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }

        Page<UserServiceDTO> selectPage = null;

        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            selectPage = userServiceService.listUserServiceByUserId(uid, page);
        } else if(RoleEnum.ROLE_SYSTEM.getMessage().equals(roleName)) {
            selectPage = userServiceService.listUserServiceByUserId(null, page);
        }
        return ResultVOUtils.success(selectPage);
    }

    @ApiOperation("获取项目服务列表")
    @GetMapping("{projectId}/list")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listServiceByProject(@RequestAttribute String uid, @PathVariable String projectId, Page<UserService> page) {

        String roleName = loginService.getRoleName(uid);

        if(StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }
        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            if(!projectService.hasBelong(projectId, uid)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }


        Page<UserService> selectPage = userServiceService.selectPage(page,
                new EntityWrapper<UserService>().eq("project_id", projectId));

        return ResultVOUtils.success(dtoConvert.convert(selectPage));
    }

}
