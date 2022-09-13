package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.VolumeTypeEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.domain.vo.SysVolumeVO;
import com.gbq.docker.uiproject.service.SysVolumeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description 数据卷控制器
 * @date 2022/9/11 12:44
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
@RestController
@Api(tags = "数据卷模块")
@RequestMapping("/volumes")
public class VolumesController {
    @Resource
    private SysVolumeService sysVolumeService;


    @ApiOperation("获取本地所有数据卷")
    @GetMapping("/list/{type}")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO listFromLocal(@PathVariable Integer type) {
        VolumeTypeEnum enums = VolumeTypeEnum.getEnum(type);
        if(enums == null) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        return sysVolumeService.listFromLocal(enums);
    }

    @ApiOperation("获取某容器的所有数据卷")
    @GetMapping("/list/obj")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listByObjId(Page<SysVolumeVO> page, @RequestAttribute String uid, String objId) {
        if(StringUtils.isBlank(objId)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        return sysVolumeService.listByObjId(page, objId, uid);
    }
}
