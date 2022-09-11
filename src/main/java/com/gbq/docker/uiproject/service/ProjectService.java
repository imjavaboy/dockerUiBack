package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.dto.UserProjectDTO;
import com.gbq.docker.uiproject.domain.entity.UserProject;
import com.gbq.docker.uiproject.domain.select.UserProjectSelect;
import com.gbq.docker.uiproject.domain.vo.ProjectLogVO;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 20:27
 * @Copyright 总有一天，会见到成功
 */

public interface ProjectService extends IService<UserProject> {

    /**
     *  获取项目列表
     * @param
     * @since 2022/9/9
     * @return
     */
    Page<UserProjectDTO> list(UserProjectSelect projectSelect, Page<UserProjectDTO> objectPage);

    /**
     *  根据id获取项目详情
     * @param
     * @since 2022/9/9
     * @return
     */
    ResultVO getProjectById(String id, String uid);

    /**
     *  根据projectId获取用户id
     * @param
     * @since 2022/9/9
     * @return
     */
    String getUserId(String projectId);

    /**
     *  获取项目日志
     * @param
     * @since 2022/9/9
     * @return
     */
    ResultVO listProjectLog(String projectId,  Page<ProjectLogVO> create_date);

     /**
      *  获取项目名
      * @param
      * @since 2022/9/9
      * @return
      */
    String getProjectName(String projectId);

    /**
     *  更新项目
     * @param
     * @since 2022/9/10
     * @return
     */
    ResultVO updateProject(String uid, String id, String name, String description);

    /**
     *  创建项目
     * @param
     * @since 2022/9/10
     * @return
     */
    ResultVO createProject(String uid, String name, String description);

    /**
     *  查询项目是否属于某个用户
     * @param
     * @since 2022/9/10
     * @return
     */
    boolean hasBelong(String projectId, String uid);
}
