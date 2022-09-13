package com.gbq.docker.uiproject.controller;


import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.entity.RepositoryImage;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.RepositoryImageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:09
 * @Copyright 总有一天，会见到成功
 */
@RestController
@RequestMapping("/hub")
@Api(tags = "hub仓储模块")
public class HubController {

    @Resource
    private RepositoryImageService repositoryImageService;


    @ApiOperation("获取镜像列表")
    @GetMapping("/list")
    public ResultVO listName() {
        return ResultVOUtils.success(repositoryImageService.listHubImageVO());
    }

    @ApiOperation("从paasHub获取镜像")
    @GetMapping("/list/name")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listTagByName(String name) {
        List<RepositoryImage> list = repositoryImageService.listByName(name);

        return ResultVOUtils.success(list);
    }



    @ApiOperation("上传镜像到hub")
    @PostMapping("/push")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO pushImage(String imageId, @RequestAttribute String uid, HttpServletRequest request) {
        // 校验
        ResultVO resultVO = repositoryImageService.pushCheck(imageId, uid);
        if(ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }

        SysImage sysImage = (SysImage) resultVO.getData();
        repositoryImageService.pushTask(sysImage, uid, request);

        return ResultVOUtils.success("开始上传镜像");
    }

    @ApiOperation("镜像同步")
    @GetMapping("/sync")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO syncImage() {
        return repositoryImageService.sync();
    }
}
