package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.dto.UserProjectDTO;
import com.gbq.docker.uiproject.domain.entity.ProjectLog;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.domain.entity.UserProject;
import com.gbq.docker.uiproject.domain.entity.UserService;
import com.gbq.docker.uiproject.domain.enums.ProjectLogTypeEnum;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.enums.SysLogTypeEnum;
import com.gbq.docker.uiproject.domain.select.UserProjectSelect;
import com.gbq.docker.uiproject.domain.vo.ProjectLogVO;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.exception.JsonException;
import com.gbq.docker.uiproject.mapper.ProjectLogMapper;
import com.gbq.docker.uiproject.mapper.UserProjectMapper;
import com.gbq.docker.uiproject.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 20:30
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class UserProjectServiceImpl extends ServiceImpl<UserProjectMapper, UserProject> implements ProjectService {

    @Value("${redis.project.key}")
    private String key;

    @Resource
    private UserProjectMapper projectMapper;

    @Resource
    private JedisClient jedisClient;

    @Resource
    private SysLoginService loginService;
    @Resource
    private ProjectLogMapper projectLogMapper;
    @Resource
    private UserServiceService userServiceService;
    @Resource
    private UserContainerService containerService;
    @Resource
    private SysLogService sysLogService;
    @Resource
    private HttpServletRequest request;

    @Resource
    private ProjectLogService projectLogService;

    @Override
    public Page<UserProjectDTO> list(UserProjectSelect projectSelect, Page<UserProjectDTO> page) {
        List<UserProjectDTO> list = projectMapper.list(projectSelect,page);
        return page.setRecords(list);
    }

    @Override
    public ResultVO getProjectById(String id, String uid) {

        if (StringUtils.isNotBlank(id)){
            String res = jedisClient.hget(key, id);
            try {
                if (StringUtils.isNotBlank(res)) {
                    UserProjectDTO userProjectDTO = JsonUtils.jsonToObject(res, UserProjectDTO.class);
                    if (userProjectDTO != null){
                        return ResultVOUtils.success(userProjectDTO);
                    }else{
                        cleanCache(id);
                    }
                }
            } catch (JsonException e) {
                log.error("缓存读取异常，错误位置：UserProjectServiceImpl.getProjectById()");
            }
            UserProjectDTO projectDTO = projectMapper.getById(id);

            if(projectDTO == null) {
                return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
            }
            //存缓存
            try {
                jedisClient.hset(key, id, JsonUtils.objectToJson(projectDTO));
            } catch (Exception e) {
                log.error("缓存存储异常，错误位置：UserProjectServiceImpl.getProjectById()");
            }

            String roleName =  loginService.getRoleName(uid);
            if (RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
                if (!uid.equals(projectDTO.getUserId())) {
                    return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
                }
            }
            return ResultVOUtils.success(projectDTO);
        }else {
            return null;
        }

    }

    @Override
    public String getUserId(String projectId) {
        try {
            String res = jedisClient.hget(key, projectId);
            if(StringUtils.isNotBlank(res)) {
                UserProjectDTO projectDTO = JsonUtils.jsonToObject(res, UserProjectDTO.class);
                return projectDTO == null ? null : projectDTO.getUserId();
            }
        } catch (Exception e) {
            log.error("缓存读取异常，错误位置：UserProjectServiceImpl.getProjectById()");
        }
        UserProjectDTO projectDTO = projectMapper.getById(projectId);
        if(projectDTO != null) {
            try {
                jedisClient.hset(key, projectId, JsonUtils.objectToJson(projectDTO));
                return projectDTO.getUserId();
            } catch (Exception e) {
                log.error("缓存存储异常，错误位置：UserProjectServiceImpl.getProjectById()");
            }
        }

       return null;
    }

    @Override
    public ResultVO listProjectLog(String projectId,  Page<ProjectLogVO> page) {
        List<ProjectLog> list = projectLogMapper.listProjectLog(projectId, page);
        List<ProjectLogVO> voList = projectLog2VO(list);

        return ResultVOUtils.success(page.setRecords(voList));

    }

    private List<ProjectLogVO> projectLog2VO(List<ProjectLog> list) {
        return list.stream().map(this::projectLog2VO).collect(Collectors.toList());
    }
    private ProjectLogVO projectLog2VO(ProjectLog log) {
        ProjectLogVO vo = new ProjectLogVO();
        BeanUtils.copyProperties(log, vo);

        String objId = log.getObjId();

        if (StringUtils.isNotBlank(objId)) {
            UserContainer container = containerService.getById(objId);
            if (container != null) {
                vo.setObjName(container.getName());
                return vo;
            }

            UserService userService = userServiceService.getById(objId);
            if (userService != null) {
                vo.setObjName(userService.getName());
                return vo;
            }
        }


        return vo;
    }

    @Override
    public String getProjectName(String projectId) {
        try {
            String res = jedisClient.hget(key, projectId);
            if(StringUtils.isNotBlank(res)) {
                UserProjectDTO projectDTO = JsonUtils.jsonToObject(res, UserProjectDTO.class);
                if(projectDTO != null) {
                    return projectDTO.getName();
                }
            }
        } catch (Exception e) {
            log.error("缓存读取异常，错误位置：UserProjectServiceImpl.getProjectName()");
        }

        UserProjectDTO projectDTO = projectMapper.getById(projectId);

        if(projectDTO == null) {
            return null;
        }

        try {
            jedisClient.hset(key, projectId, JsonUtils.objectToJson(projectDTO));
        } catch (Exception e) {
            log.error("缓存存储异常，错误位置：UserProjectServiceImpl.getProjectById()");
        }
        return projectDTO.getName();
    }

    @Override
    public ResultVO updateProject(String uid, String id, String name, String description) {
        ResultVO resultVO = getProjectById(id, uid);
        if(resultVO.getCode() != ResultEnum.OK.getCode()) {
            return resultVO;
        }

        UserProject project = (UserProject) resultVO.getData();

        if(StringUtils.isNotBlank(name)) {
            project.setName(name);
        }
        if(StringUtils.isNotBlank(description)) {
            project.setDescription(description);
        }

        projectMapper.updateById(project);
        // 清理缓存
        cleanCache(id);

        return ResultVOUtils.success();
    }

    @Override
    public ResultVO createProject(String uid, String name, String description) {
        if(StringUtils.isBlank(uid, name)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        UserProject project = new UserProject();
        project.setUserId(uid);
        project.setName(name);
        if(StringUtils.isNotBlank(description)) {
            project.setDescription(description);
        }

        try {
            projectMapper.insert(project);
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.CREATE_PROJECT,null);
            projectLogService.saveSuccessLog(project.getId(),null, ProjectLogTypeEnum.CREATE_PROJECT);

            return ResultVOUtils.success();
        } catch (Exception e) {
            log.error("创建项目出现错误，错误位置：{}，错误栈：{}",
                    "UserProjectServiceImpl.createProject()", HttpClientUtils.getStackTraceAsString(e));
            // 写入日志
            sysLogService.saveLog(request, SysLogTypeEnum.CREATE_PROJECT, e);

            return ResultVOUtils.error(ResultEnum.CREATE_PROJECT_ERROR);
        }
    }

    @Override
    public boolean hasBelong(String projectId, String uid) {
        return projectMapper.hasBelong(projectId,uid);
    }
//    @Override
    public void cleanCache(String id) {
        try {
            // 清理项目缓存
            jedisClient.hdel(key, id);

            // 更新所属容器
            List<UserContainer> containers = containerService.selectList(new EntityWrapper<UserContainer>().eq("project_id",id));
            for(UserContainer container : containers) {
                container.setProjectId(null);
                containerService.updateById(container);
            }
            // 更新所属服务
            List<UserService> services = userServiceService.selectList(new EntityWrapper<UserService>().eq("project_id",id));
            for(UserService service : services) {
                service.setProjectId(null);
                userServiceService.updateById(service);
                userServiceService.cleanCache(service.getId());
            }
        } catch (Exception e) {
            log.error("缓存删除异常，错误位置：UserProjectServiceImpl.cleanCache()");
        }
    }
}
