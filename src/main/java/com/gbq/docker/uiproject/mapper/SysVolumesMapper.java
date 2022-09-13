package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.gbq.docker.uiproject.domain.entity.SysVolume;
import com.gbq.docker.uiproject.domain.vo.SysVolumeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:44
 * @Copyright 总有一天，会见到成功
 */
public interface SysVolumesMapper extends BaseMapper<SysVolume> {
    void deleteByObjId(@Param("objId") String objId);


    List<SysVolume> selectByObjId(@Param("objId") String objId, Pagination page);
}
