package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.gbq.docker.uiproject.domain.entity.ProjectLog;
import com.gbq.docker.uiproject.domain.vo.ProjectLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:33
 * @Copyright 总有一天，会见到成功
 */
public interface ProjectLogMapper extends BaseMapper<ProjectLog> {
    /**
     *  或企业项目的日志
     * @param
     * @since 2022/9/9
     * @return
     */
    List<ProjectLog> listProjectLog(@Param("projectId") String projectId, Pagination page);
}
