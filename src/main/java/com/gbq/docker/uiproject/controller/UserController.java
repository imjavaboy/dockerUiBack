package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.component.WrapperComponent;
import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.select.UserSelect;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.JwtService;
import com.gbq.docker.uiproject.service.SysLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/7
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
@Api(tags = "用户模块")
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private SysLoginService sysLoginService;
    @Resource
    private JwtService jwtService;
    @Resource
    private WrapperComponent wrapperComponent;



    @ApiOperation("获取用户列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO getUserList(UserSelect userSelect, Page<SysLogin> page){
        EntityWrapper<SysLogin> entityWrapper = wrapperComponent.genUserWrapper(userSelect);
        Page<SysLogin> sysLoginPage = sysLoginService.selectPage(page, entityWrapper);
        return ResultVOUtils.success(sysLoginPage);
    }



    @ApiOperation("退出登录")
    @GetMapping("/logout")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO logout(@RequestAttribute String uid){
        try {
            SysLogin sysLogin = sysLoginService.getById(uid);
            jwtService.deleteToken(sysLogin.getUsername());
            return ResultVOUtils.success();
        } catch (Exception e) {
            log.error("退出登录失败，错误位置：{}，错误栈：{}", "UserController.logout()", HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.OTHER_ERROR);
        }
    }

    @ApiOperation("冻结用户")
    @PostMapping("/freeze")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO freezeUser(String[] ids) {
        if(ids == null || ids.length == 0) {
            return ResultVOUtils.error(ResultEnum.FREEZE_USER_ERROR);
        }

        int count = sysLoginService.freezeUser(ids);

        return ResultVOUtils.success(count);
    }

    @PostMapping("/cancelFreeze")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO cancelFreezeUser(String[] ids) {
        if(ids == null || ids.length == 0) {
            return ResultVOUtils.error(ResultEnum.FREEZE_USER_ERROR);
        }

        int count = sysLoginService.cancelFreezeUser(ids);

        return ResultVOUtils.success(count);
    }

}
