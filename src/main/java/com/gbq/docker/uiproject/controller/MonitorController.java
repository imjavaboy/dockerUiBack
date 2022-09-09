package com.gbq.docker.uiproject.controller;


import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 9:04
 * @Copyright 总有一天，会见到成功
 */
@Api(tags = "监控模块")
@RestController
@RequestMapping("/monitor")
@Slf4j
public class MonitorController {

    @Resource
    private MonitorService monitorService;

    @ApiOperation("读取docker宿主机信息")
    @GetMapping("/host")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO getHostInfo(){
        return monitorService.getDockerInfo();
    }


    @ApiOperation("获取自身docker的信息")
    @GetMapping("/self/info")
    public ResultVO getSelfDockerInfo(@RequestAttribute String uid){
        return monitorService.getUserDockerInfo(uid);
    }
}
