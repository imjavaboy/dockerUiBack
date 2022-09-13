package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.domain.entity.SysNetwork;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:36
 * @Copyright 总有一天，会见到成功
 */

public interface SysNetworkMapper extends BaseMapper<SysNetwork> {

    List<SysNetwork> listAllNetwork(Page<SysNetwork> page,@Param("hasPublic") Boolean hasPublic);

    List<SysNetwork> listSelfAndPublicNetwork(Page<SysNetwork> page,@Param("uid") String uid);

    List<SysNetwork> listSelfNetwork(Page<SysNetwork> page,@Param("uid") String uid);
}
