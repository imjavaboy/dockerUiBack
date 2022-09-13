package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.entity.ProjectLog;
import com.gbq.docker.uiproject.domain.enums.ProjectLogTypeEnum;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/10 10:14
 * @Copyright 总有一天，会见到成功
 */

public interface ProjectLogService extends IService<ProjectLog> {

    /**
     *  保存项目操作日志
     * @param
     * @since 2022/9/10
     * @return
     */
    void saveSuccessLog(String id, String objId, ProjectLogTypeEnum createProject);


    /**
     *  保存操作失败的日志
     * @param
     * @since 2022/9/12
     * @return
     */
    void saveErrorLog(String projectId, String id, ProjectLogTypeEnum createContainerError, ResultEnum dockerException);
}
