package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.component.UserContainerDTOConvert;
import com.gbq.docker.uiproject.domain.dto.UserContainerDTO;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import com.gbq.docker.uiproject.mapper.UserContainerMapper;
import com.gbq.docker.uiproject.service.UserContainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:54
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class UserContainerServiceImpl extends ServiceImpl<UserContainerMapper, UserContainer> implements UserContainerService {
    @Resource
    private UserContainerDTOConvert dtoConvert;
    @Resource
    private UserContainerMapper userContainerMapper;

    @Override
    public UserContainerDTO getById(String objId) {
        return dtoConvert.convert(userContainerMapper.selectById(objId));
    }

    @Override
    public Page<UserContainerDTO> listContainerByUserId(String uid, String name, Integer status, Page<UserContainer> page) {

        List<UserContainer> containers = userContainerMapper.listContainerByUserIdAndNameAndStatus(page,uid,name,status);
        Page<UserContainerDTO> page1 = new Page<>();
        BeanUtils.copyProperties(page, page1);
        return page1.setRecords(dtoConvert.convert(containers));
    }
}
