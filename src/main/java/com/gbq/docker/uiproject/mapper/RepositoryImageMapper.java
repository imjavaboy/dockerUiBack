package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.gbq.docker.uiproject.domain.entity.RepositoryImage;
import com.gbq.docker.uiproject.domain.vo.HubImageVO;

import java.util.List;

/**
 * @author 郭本琪
 * @description 仓储镜像接口
 * @date 2022/9/9 11:33
 * @Copyright 总有一天，会见到成功
 */

public interface RepositoryImageMapper extends BaseMapper<RepositoryImage> {

    List<HubImageVO> listHubImageVO();
}
