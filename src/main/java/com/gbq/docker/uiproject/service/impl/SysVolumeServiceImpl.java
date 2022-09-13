package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.entity.SysVolume;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.domain.entity.UserService;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.enums.VolumeTypeEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.domain.vo.SysVolumeVO;
import com.gbq.docker.uiproject.exception.CustomException;
import com.gbq.docker.uiproject.mapper.SysVolumesMapper;
import com.gbq.docker.uiproject.mapper.UserContainerMapper;
import com.gbq.docker.uiproject.mapper.UserServiceMapper;
import com.gbq.docker.uiproject.service.SysLoginService;
import com.gbq.docker.uiproject.service.SysVolumeService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:46
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class SysVolumeServiceImpl extends ServiceImpl<SysVolumesMapper, SysVolume> implements SysVolumeService {

    @Resource
    private DockerClient dockerClient;
    @Resource
    private DockerClient dockerSwarmClient;
    @Resource
    private SysLoginService loginService;
    @Resource
    private UserContainerMapper containerMapper;
    @Resource
    private UserServiceMapper serviceMapper;
    @Resource
    private SysVolumesMapper volumesMapper;

    @Override
    public ResultVO listFromLocal(VolumeTypeEnum enums) {
        try {
            VolumeList volumeList;

            // 根据数据卷类型查询
            if(enums == VolumeTypeEnum.CONTAINER) {
                volumeList = dockerClient.listVolumes();
            } else if(enums == VolumeTypeEnum.SERVICE) {
                volumeList = dockerSwarmClient.listVolumes();
            } else {
                throw new CustomException(ResultEnum.OTHER_ERROR.getCode(), "数据卷类型不被接收");
            }

            return ResultVOUtils.success(volumeList);
        } catch (Exception e) {
            log.error("获取本地数据卷异常，错误位置：{}，错误栈：{}",
                    "SysVolumeServiceImpl.listFromLocal()", HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.VOLUME_LIST_ERROR);
        }
    }

    @Override
    public ResultVO listByObjId(Page<SysVolumeVO> page, String objId, String uid) {
        // 鉴权
        String roleName = loginService.getRoleName(uid);
        if(StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }
        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            // 判断Obj类型
            UserContainer container = containerMapper.selectById(objId);
            UserService service = null;
            if(container == null) {
                service = serviceMapper.selectById(objId);
            }

            if(container == null && service == null) {
                return ResultVOUtils.success(ResultEnum.VOLUME_OBJ_NOT_EXIST);
            }

            if(container != null && !containerMapper.hasBelongSb(objId, uid)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            } else if(service != null && !serviceMapper.hasBelong(objId, uid)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }

        // 查询
        try {
            List<SysVolumeVO> voList = sysVolume2VO(volumesMapper.selectByObjId(objId, page));
            return ResultVOUtils.success(page.setRecords(voList));
        } catch (Exception e) {
            return ResultVOUtils.error(ResultEnum.VOLUME_INFO_ERROR.getCode(), e.getMessage());
        }
    }
    private List<SysVolumeVO> sysVolume2VO(List<SysVolume> sysVolumes) throws Exception{
        List<SysVolumeVO> list = new ArrayList<>();

        for(SysVolume sysVolume : sysVolumes) {
            list.add(sysVolume2VO(sysVolume));
        }
        return list;
    }
    private SysVolumeVO sysVolume2VO(SysVolume sysVolume) throws Exception {
        Volume volume;
        SysVolumeVO volumeVO = new SysVolumeVO();
        int type = sysVolume.getType();

        if(type == VolumeTypeEnum.CONTAINER.getCode()) {
            volume = dockerClient.inspectVolume(sysVolume.getName());

            UserContainer container = containerMapper.selectById(sysVolume.getObjId());
            if(container != null) {
                volumeVO.setObjName(container.getName());
            }
        } else if(type == VolumeTypeEnum.SERVICE.getCode()) {
            volume = dockerSwarmClient.inspectVolume(sysVolume.getName());

            UserService service = serviceMapper.selectById(sysVolume.getObjId());
            if(service != null) {
                volumeVO.setObjName(service.getName());
            }
        } else {
            throw new CustomException(ResultEnum.OTHER_ERROR.getCode(), "数据卷类型不被接受");
        }

        BeanUtils.copyProperties(sysVolume, volumeVO);

        volumeVO.setVolume(volume);
        volumeVO.setTypeName(VolumeTypeEnum.getMessage(type));

        return volumeVO;
    }
}

