package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.gbq.docker.uiproject.domain.dto.UserProjectDTO;
import com.gbq.docker.uiproject.domain.entity.UserProject;
import com.gbq.docker.uiproject.domain.select.UserProjectSelect;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 11:38
 * @Copyright 总有一天，会见到成功
 */
public interface UserProjectMapper extends BaseMapper<UserProject> {

    /**
     *  查询列表
     * @param
     * @since 2022/9/9
     * @return
     */
    List<UserProjectDTO> list(@Param("projectSelect") UserProjectSelect projectSelect, Pagination page);

    UserProjectDTO getById(String id);

    /**
     *  查询项目是否属于某人
     * @param
     * @since 2022/9/10
     * @return
     */
    boolean hasBelong(@Param("projectId") String projectId, @Param("uid") String uid);
}
