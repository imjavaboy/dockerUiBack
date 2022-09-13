package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.convert.UserServiceDTOConvert;
import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.dto.UserServiceDTO;
import com.gbq.docker.uiproject.domain.entity.UserService;
import com.gbq.docker.uiproject.mapper.UserServiceMapper;
import com.gbq.docker.uiproject.service.UserServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 21:43
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
@Service
public class UserServiceServiceImpl extends ServiceImpl<UserServiceMapper, UserService> implements UserServiceService {
    @Value("${redis.user-service.key}")
    private String key;


    @Resource
    private JedisClient jedisClient;
    @Resource
    private UserServiceDTOConvert dtoConvert;
    @Resource
    private UserServiceMapper userServiceMapper;


    @Override
    public UserService getById(String objId) {
        try {
            String json = jedisClient.hget(key, objId);
            if(StringUtils.isNotBlank(json)) {
                return JsonUtils.jsonToObject(json, UserServiceDTO.class);
            }
        } catch (Exception e) {
            log.error("缓存读取异常，异常位置：{}","UserServiceServiceImpl.getById()");
        }

        UserServiceDTO serviceDTO = dtoConvert.convert(userServiceMapper.selectById(objId));
        if(serviceDTO == null) {
            return null;
        }

        try {
            jedisClient.hset(key, objId, JsonUtils.objectToJson(serviceDTO));
        } catch (Exception e) {
            log.error("缓存存储异常，异常位置：{}", "UserServiceServiceImpl.getById()");
        }

        return serviceDTO;
    }

    @Override
    public Page<UserServiceDTO> listUserServiceByUserId(String uid, Page<UserService> page) {
        List<UserService> services = userServiceMapper.listServiceByUserId(page,uid);

        Page<UserServiceDTO> page1 = new Page<>();
        BeanUtils.copyProperties(page, page1);
        return page1.setRecords(dtoConvert.convert(services));
    }
    @Override
    public void cleanCache(String id) {
        try {
            jedisClient.hdel(key, id);
        } catch (Exception e) {
            log.error("缓存清理异常，异常位置：{}", "UserServiceServiceImpl.cleanCache()");
        }
    }
}
