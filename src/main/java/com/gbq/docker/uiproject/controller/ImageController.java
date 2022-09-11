package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.dto.SysImageDTO;
import com.gbq.docker.uiproject.domain.enums.ImageTypeEnum;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.SysImageService;
import com.gbq.docker.uiproject.service.SysLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/10 9:23
 * @Copyright 总有一天，会见到成功
 */
@Api(tags = "镜像模块")
@RestController
@RequestMapping("/image")
public class ImageController {

    @Resource
    private SysLoginService loginService;
    @Resource
    private SysImageService imageService;

    @ApiOperation("查找本地镜像")
    @GetMapping("/list/local")
    public ResultVO searchLocalImage(@RequestAttribute String uid, String name, Integer type,
                                     @RequestParam(defaultValue = "1") Integer current,
                                     @RequestParam(defaultValue = "10") Integer size){
        if(type == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        Page<SysImageDTO> page = new Page<>(current, size, "create_date", false);

        String roleName = loginService.getRoleName(uid);

        if (type == ImageTypeEnum.LOCAL_PUBLIC_IMAGE.getCode()) {
            // 本地公共镜像
            return ResultVOUtils.success(imageService.listLocalPublicImage(name, page));
        } else if (type == ImageTypeEnum.LOCAL_USER_IMAGE.getCode()) {
            // 系统管理员查看所有本地用户镜像，普通用户只能查看公开的本地用户镜像和自己镜像
            if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
                return ResultVOUtils.success(imageService.listLocalUserImage(name, true, uid, page));
            } else {
                return ResultVOUtils.success(imageService.listLocalUserImage(name, false, uid, page));
            }

        } else {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
    }

    @ApiOperation("查找hub上的镜像")
    @GetMapping("/list/hub")
    public ResultVO searchHubImage(String name, @RequestParam(required = false, defaultValue = "10") int limit) {
        return imageService.listHubImage(name, limit);
    }

}
