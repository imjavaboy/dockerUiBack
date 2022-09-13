package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.entity.SysNetwork;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.SysNetworkService;
import com.gbq.docker.uiproject.service.UserContainerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:34
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
@RestController
@RequestMapping("/network")
@Api(tags = "网络模块")
public class NetWorkController {

    @Resource
    private SysNetworkService networkService;
    @Resource
    private UserContainerService containerService;


    @ApiOperation("获取网络列表(管理员)")
    @GetMapping("/listAll")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO listAllNetwork( @ApiParam("是否公供") Boolean hasPublic,
                                   @RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size) {
        Page<SysNetwork> page = new Page<>(current,size,"create_date",false);
        return ResultVOUtils.success(networkService.listAllNetwork(page, hasPublic));
    }
    @ApiOperation("获取网络列表(用户)")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResultVO listNetwork(@RequestParam(defaultValue = "-1") @ApiParam("类型：1：公共网络；2：个人网络；其他：个人网络 + 公共网络")
                                            int type, @RequestAttribute String uid,
                                @RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<SysNetwork> page = new Page<>(current,size,"create_date",false);
        switch (type) {
            case 1:
                return ResultVOUtils.success(networkService.listAllNetwork(page, true));
            case 2:
                return ResultVOUtils.success(networkService.listSelfNetwork(page, uid));
            default:
                return ResultVOUtils.success(networkService.listSelfAndPublicNetwork(page, uid));
        }
    }
    @ApiOperation("获取某一容器的所有网络")
    @GetMapping("/container/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listByContainerId(@PathVariable String id, @RequestAttribute String uid) {
        // 鉴权
        ResultVO resultVO = containerService.checkPermission(uid, id);
        if(ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }

        return networkService.listByContainerId(id);
    }

    @ApiOperation("获取网络详情")
    @GetMapping("/{id}")
    public ResultVO getById(@PathVariable String id, @RequestAttribute String uid) {
        ResultVO resultVO = networkService.hasPermission(id, uid);
        if(ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }
        return ResultVOUtils.success(networkService.getById(id));
    }

}
