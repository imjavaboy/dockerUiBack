package com.gbq.docker.uiproject.controller;


import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.domain.vo.UserVO;
import com.gbq.docker.uiproject.service.JwtService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 郭本琪
 * @description token管理
 * @date 2022/9/9 13:54
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
@RestController
@RequestMapping("/token")
@Api(tags = "token模块")
public class TokenController {
    @Autowired
    private JwtService jwtService;

    @PostMapping("")
    @ApiOperation("获取用户信息")
    public ResultVO getUserInfo(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        UserVO userInfo = jwtService.getUserInfo(token);

        return ResultVOUtils.success(userInfo);
    }


    @ApiOperation("获取所有token")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO listToken() {
        return jwtService.listToken();
    }

    @ApiOperation("删除token")
    @DeleteMapping("/delete/{username}")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO deleteToken(@PathVariable String username) {
        if(StringUtils.isBlank(username)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        return jwtService.deleteToken(username);
    }
}
