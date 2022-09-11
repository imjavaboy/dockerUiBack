package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.UserServiceDTO;
import com.gbq.docker.uiproject.domain.entity.UserService;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.gbq.docker.uiproject.service.UserServiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
