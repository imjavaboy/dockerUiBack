package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.UserProjectDTO;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.select.UserProjectSelect;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.ProjectService;
import com.gbq.docker.uiproject.service.SysLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description 项目控制器
 * @date 2022/9/9 14:16
 * @Copyright 总有一天，会见到成功
 */
@RestController
@RequestMapping("/project")
@Api(tags = "项目模块")
public class ProjectController {

    @Resource
    private ProjectService projectService;
    @Resource
    private SysLoginService loginService;

    @ApiOperation("获取所有项目列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public ResultVO listProject(UserProjectSelect projectSelect,
                                @RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<UserProjectDTO> selectPage = projectService.list(projectSelect, new Page<>(current, size));
        return ResultVOUtils.success(selectPage);
    }

    @ApiOperation("根据Id查询项目")
    @GetMapping("/{id}")
    public ResultVO getProjectById(@RequestAttribute String uid, @PathVariable String id) {
        return projectService.getProjectById(id, uid);
    }

    @ApiOperation("获取项目操作日志")
    @GetMapping("/log")
    public ResultVO getProjectLog(@RequestAttribute String uid, String projectId,
                                  @RequestParam(defaultValue = "1") Integer current,
                                  @RequestParam(defaultValue = "10") Integer size) {
        if(StringUtils.isBlank(projectId)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        // 鉴权
        String roleName = loginService.getRoleName(uid);
        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            System.out.println("hhhhhhh"+projectId);
            String userId = projectService.getUserId(projectId);

            if(!uid.equals(userId)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }

        return projectService.listProjectLog(projectId,  new Page<>(current, size, "create_date", false));
    }


    @ApiOperation("用户项目列表")
    @GetMapping("/self/list")
    public ResultVO listSelfProject(@RequestAttribute String uid, UserProjectSelect projectSelect,
                                    @RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size) {
        // 设置筛选条件userId为当前用户
        projectSelect.setUserId(uid);
        // 设置筛选条件username为null
        projectSelect.setUsername(null);

        Page<UserProjectDTO> selectPage = projectService.list(projectSelect, new Page<>(current, size));
        return ResultVOUtils.success(selectPage);
    }
    @ApiOperation("更新项目")
    @PutMapping("/update")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResultVO updateProject(@RequestAttribute String uid, String id, String name, String description) {
        return projectService.updateProject(uid, id, name, description);
    }

    @ApiOperation("创建项目")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResultVO createProject(@RequestAttribute String uid, String name, String description) {
        return projectService.createProject(uid, name, description);
    }


}
