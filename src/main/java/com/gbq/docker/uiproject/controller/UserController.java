package com.gbq.docker.uiproject.controller;


import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.JwtService;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.github.xiaoymin.swaggerbootstrapui.io.ResourceUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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


    @GetMapping("/getbyId")
    public ResultVO getSelfInfo(String id){
        SysLogin sysLogin = sysLoginService.getById(id);
        return ResultVOUtils.success(sysLogin);
    }

//    @PostMapping("/freeze")
//    @PreAuthorize("hasRole('ROLE_SYSTEM')")
//    public ResultVO freezeUser(String)

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

}
