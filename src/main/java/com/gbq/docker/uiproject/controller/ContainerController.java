package com.gbq.docker.uiproject.controller;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.commons.component.UserContainerDTOConvert;
import com.gbq.docker.uiproject.commons.util.CollectionUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.dto.UserContainerDTO;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.domain.enums.ContainerOpEnum;
import com.gbq.docker.uiproject.domain.enums.ContainerStatusEnum;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.ProjectService;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.gbq.docker.uiproject.service.UserContainerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 郭本琪
 * @description 容器模块
 * @date 2022/9/9 22:43
 * @Copyright 总有一天，会见到成功
 */
@Api(tags = "容器模块")
@Slf4j
@RestController
@RequestMapping("/container")
public class ContainerController {


    @Resource
    private SysLoginService loginService;
    @Resource
    private UserContainerService containerService;
    @Resource
    private ProjectService projectService;
    @Resource
    private UserContainerDTOConvert dtoConvert;


    @Value("${docker.server.address}")
    private String dockerAddress;
    @Value("${docker.server.port}")
    private String dockerPort;
    @Value("${server.ip}")
    private String serverIp;
    @Value("${server.port}")
    private String serverPort;

    @ApiOperation("根据容器获取容器")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO getById(@RequestAttribute String uid, @PathVariable String id) {
        ResultVO resultVO = containerService.checkPermission(uid, id);
        if (ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }
        UserContainerDTO containerDTO = containerService.getById(id);

        return ResultVOUtils.success(containerDTO);
    }

    @ApiOperation("获取所有容器")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listContainer(@RequestAttribute String uid, String name, Integer status,
                                  @RequestParam(defaultValue = "1") Integer current,
                                  @RequestParam(defaultValue = "10") Integer size) {
        // 鉴权
        String roleName = loginService.getRoleName(uid);
        // 角色无效
        if (StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }

        Page<UserContainer> page = new Page<>(current, size, "update_date", false);
        Page<UserContainerDTO> selectPage = null;

        if (RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            selectPage = containerService.listContainerByUserId(uid, name, status, page);
        } else if (RoleEnum.ROLE_SYSTEM.getMessage().equals(roleName)) {
            selectPage = containerService.listContainerByUserId(null, name, status, page);
        }

        return ResultVOUtils.success(selectPage);
    }

    @ApiOperation("获取容器内所有的项目，普通用户获取本人项目的容器，系统管理员任意项目的容器")
    @GetMapping("/project/{projectId}/list")
    public ResultVO listContainerByProject(@RequestAttribute String uid, @PathVariable String projectId, Page<UserContainer> page) {
        String roleName = loginService.getRoleName(uid);

        if (StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }
        if (RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            if (!projectService.hasBelong(projectId, uid)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }

        Page<UserContainer> pageList = containerService.selectPage(page, new EntityWrapper<UserContainer>().eq("project_id", projectId));
        return ResultVOUtils.success(dtoConvert.convert(pageList));

    }

    @ApiOperation("获取容器内部状态")
    @GetMapping("/top/{containerId}")
    public ResultVO topContainer(@RequestAttribute String uid, @PathVariable String containerId) {
        return containerService.topContainer(uid, containerId);
    }
    /**
     *
     * @param imageId        镜像ID 必填
     * @param containerName  容器名 必填
     * @param projectId      所属项目 必填
     * @param portMapStr     端口映射，Map<String,String> JSON字符串
     * @param cmdStr         执行命令，如若为空，使用默认的命令，多个分号连接
     * @param envStr         环境变量，多个分号连接
     * @param destinationStr 容器内部目录，多个分号连接
     * @since 2022/9/12
     * @return
     */
    @ApiOperation("创建容器")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResultVO createContainer(String imageId, String containerName, String projectId,
                                    String portMapStr, String cmdStr, String envStr, String destinationStr,
                                    @RequestAttribute String uid, HttpServletRequest request) {
        if (StringUtils.isBlank(imageId, containerName, projectId)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        //端口映射的转换
        Map<String, String> portMap;
        try {
            portMap = CollectionUtils.mapJson2map(portMapStr);
        } catch (Exception e) {
            log.error("Json格式解析错误，错误位置：{}，错误信息：{}", "ContainerController.createContainer()", e.getMessage());
            return ResultVOUtils.error(ResultEnum.JSON_ERROR);
        }
        String[] cmd = CollectionUtils.str2Array(cmdStr, ";"),
                env = CollectionUtils.str2Array(envStr, ";"),
                destination = CollectionUtils.str2Array(destinationStr, ";");
        //校验
        ResultVO resultVO = containerService.createContainerCheck(uid, imageId, portMap, projectId);
        if (ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        } else {
            containerService.createContainerTask(uid, imageId, cmd, portMap, containerName, projectId, env, destination, request);
            return ResultVOUtils.success("开始创建容器");
        }

    }

    @ApiOperation("获取容器状态")
    @GetMapping("/status/{id}")
    public ResultVO getStatus(@PathVariable String id) {
        ContainerStatusEnum status = containerService.getStatus(id);

        return ResultVOUtils.success(status.getCode());
    }
    @ApiOperation("开启容器，通过websocket")
    @GetMapping("/start/{containerId}")
    public ResultVO startContainer(@RequestAttribute String uid, @PathVariable String containerId) {
        //判断操作是否被允许
        ResultVO resultVO = containerService.hasAllowOp(uid, containerId, ContainerOpEnum.START);

        if (ResultEnum.OK.getCode() == resultVO.getCode()) {
            containerService.startContainerTask(uid, containerId);
            return ResultVOUtils.success("开始启动容器");
        } else {
            return resultVO;
        }
    }
    @ApiOperation("暂停容器")
    @GetMapping("/pause/{containerId}")
    public ResultVO pauseContainer(@RequestAttribute String uid, @PathVariable String containerId) {
        ResultVO resultVO = containerService.hasAllowOp(uid, containerId, ContainerOpEnum.PAUSE);

        if (ResultEnum.OK.getCode() == resultVO.getCode()) {
            containerService.pauseContainerTask(uid, containerId);
            return ResultVOUtils.success("开始暂停容器");
        } else {
            return resultVO;
        }
    }

    @ApiOperation("重启容器")
    @GetMapping("/restart/{containerId}")
    public ResultVO restartContainer(@RequestAttribute String uid, @PathVariable String containerId) {
        ResultVO resultVO = containerService.hasAllowOp(uid, containerId, ContainerOpEnum.RESTART);

        if (ResultEnum.OK.getCode() == resultVO.getCode()) {
            containerService.restartContainerTask(uid, containerId);
            return ResultVOUtils.success("开始重启容器");
        } else {
            return resultVO;
        }
    }
    @ApiOperation("停止容器")
    @GetMapping("/stop/{containerId}")
    public ResultVO stopContainer(@RequestAttribute String uid, @PathVariable String containerId) {
        ResultVO resultVO = containerService.hasAllowOp(uid, containerId, ContainerOpEnum.STOP);

        if (ResultEnum.OK.getCode() == resultVO.getCode()) {
            containerService.stopContainerTask(uid, containerId);
            return ResultVOUtils.success("开始停止容器");
        } else {
            return resultVO;
        }
    }
    @ApiOperation("强制停止容器")
    @GetMapping("/kill/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO killContainer(@RequestAttribute String uid, @PathVariable String containerId) {
        ResultVO resultVO = containerService.hasAllowOp(uid, containerId, ContainerOpEnum.KILL);

        if (ResultEnum.OK.getCode() == resultVO.getCode()) {
            containerService.killContainerTask(uid, containerId);
            return ResultVOUtils.success("开始强制停止容器");
        } else {
            return resultVO;
        }
    }
    @ApiOperation("暂停状态恢复容器")
    @GetMapping("/continue/{containerId}")
    public ResultVO continueContainer(@RequestAttribute String uid, @PathVariable String containerId) {
        ResultVO resultVO = containerService.hasAllowOp(uid, containerId, ContainerOpEnum.CONTINUE);

        if (ResultEnum.OK.getCode() == resultVO.getCode()) {
            containerService.continueContainerTask(uid, containerId);
            return ResultVOUtils.success("开始恢复容器");
        } else {
            return resultVO;
        }
    }
    @ApiOperation("删除容器")
    @DeleteMapping("/delete/{containerId}")
    public ResultVO removeContainer(@PathVariable String containerId, @RequestAttribute String uid, HttpServletRequest request) {
        ResultVO resultVO = containerService.hasAllowOp(uid, containerId, ContainerOpEnum.DELETE);

        if (ResultEnum.OK.getCode() == resultVO.getCode()) {
            containerService.removeContainerTask(uid, containerId, request);
            return ResultVOUtils.success("开始删除容器");
        } else {
            return resultVO;
        }
    }
    @ApiOperation("同步容器")
    @GetMapping("/sync")
    public ResultVO sync(@RequestAttribute String uid) {
        String roleName = loginService.getRoleName(uid);

        containerService.sync();
//        if (RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
//            return ResultVOUtils.success(containerService.syncStatus(uid));
//        } else if (RoleEnum.ROLE_SYSTEM.getMessage().equals(roleName)) {
//            return ResultVOUtils.success(containerService.syncStatus(null));
//        } else {
//            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
//        }

        return ResultVOUtils.success();
    }


    @ApiOperation("同步容器状态")
    @GetMapping("/syncStatus")
    public ResultVO syncStatus(@RequestAttribute String uid) {
        String roleName = loginService.getRoleName(uid);
//
//        containerService.sync();
        if (RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            return ResultVOUtils.success(containerService.syncStatus(uid));
        } else if (RoleEnum.ROLE_SYSTEM.getMessage().equals(roleName)) {
            return ResultVOUtils.success(containerService.syncStatus(null));
        } else {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }

    }

    @ApiOperation("修改容器所属的项目")
    @PostMapping("/changeProject")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResultVO changeBelongProject(String containerId, String projectId, @RequestAttribute String uid) {
        if (StringUtils.isBlank(containerId, projectId)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        return containerService.changeBelongProject(containerId, projectId, uid);
    }

    @ApiOperation("调用终端")
    @PostMapping("/terminal")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO showTerminal(@RequestAttribute String uid, String containerId,
                                 @RequestParam(defaultValue = "false") Boolean cursorBlink,
                                 @RequestParam(defaultValue = "100") Integer cols,
                                 @RequestParam(defaultValue = "50") Integer rows,
                                 @RequestParam(defaultValue = "100") Integer width,
                                 @RequestParam(defaultValue = "50") Integer height) {
        UserContainer container = containerService.getById(containerId);
        if (container == null) {
            return ResultVOUtils.error(ResultEnum.CONTAINER_NOT_FOUND);
        }

        // 只有启动状态容器才能调用Terminal
        ContainerStatusEnum status = containerService.getStatus(containerId);
        if (status != ContainerStatusEnum.RUNNING) {
            return ResultVOUtils.error(ResultEnum.CONTAINER_NOT_RUNNING);
        }

        // 鉴权
        ResultVO resultVO = containerService.checkPermission(uid, containerId);
        if (ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }

        String url = "ws://" + serverIp + ":" + serverPort + "/ws/container/exec?width=" + width + "&height=" + height +
                "&ip=" + dockerAddress + "&port=" + dockerPort + "&containerId=" + containerId;

        Map<String, Object> map = new HashMap<>(16);
        map.put("cursorBlink", cursorBlink);
        map.put("cols", cols);
        map.put("rows", rows);
        map.put("url", url);
        return ResultVOUtils.success(map);
    }


}
