package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.entity.SysVolume;
import com.gbq.docker.uiproject.domain.enums.VolumeTypeEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.domain.vo.SysVolumeVO;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:46
 * @Copyright 总有一天，会见到成功
 */
public interface SysVolumeService extends IService<SysVolume> {

    /**
     *  或去本地数据卷
     * @param
     * @since 2022/9/11
     * @return
     */
    ResultVO listFromLocal(VolumeTypeEnum enums);

    /**
     *  获取挂载列表
     * @param
     * @since 2022/9/13
     * @return
     */
    ResultVO listByObjId(Page<SysVolumeVO> page, String objId, String uid);
}
