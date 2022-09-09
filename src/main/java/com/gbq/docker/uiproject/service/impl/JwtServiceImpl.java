package com.gbq.docker.uiproject.service.impl;


import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.JwtUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.domain.vo.UserVO;
import com.gbq.docker.uiproject.service.JwtService;
import com.gbq.docker.uiproject.service.SysLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 郭本琪
 * @description jwt的工具服务
 * @date 2022/9/8 12:15
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
    @Resource
    private SysLoginService loginService;
    @Resource
    private JedisClient jedisClient;

    @Value("${redis.token.key}")
    private String key;

    /**
     * JWT有效时间（单位：小时）
     */
    @Value("${redis.token.expire}")
    private Integer expireHour;

    @Override
    public String genToken(String username) {
        SysLogin loginUser = loginService.getByUserName(username);

        Map<String,Object> map = new HashMap<>();
        map.put("uid", loginUser.getId());
        map.put("rid", loginUser.getRoleId());
        map.put("timestamp", System.currentTimeMillis());
        String token = JwtUtils.sign(map, 6 * 3600 * 1000);

        try {
            jedisClient.hdel(key, username);
            jedisClient.hset(key, username, token);

            return token;
        } catch (Exception e) {
            log.error("token缓存出现错误，错误位置：{}，错误栈：{}", "JwtServiceImpl.genToken()",HttpClientUtils.getConnManagerStats());
            return null;
        }

    }



    @Override
    public UserVO getUserInfo(String token) {
        Map map = JwtUtils.unSign(token);
        String userId = (String)map.get("uid");
        Integer roleId = (Integer)map.get("rid");

        SysLogin login = loginService.selectById(userId);

        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setRoleId(roleId);
        userVO.setUsername(login.getUsername());
        userVO.setEmail(login.getEmail());

        return userVO;
    }

    @Override
    public ResultVO checkToken(String token) {
        try {
            // 1、判断Token是否存在于Redis
            List<String> tokens = jedisClient.hvals(key);
            if(!tokens.contains(token)) {
                return ResultVOUtils.error(ResultEnum.TOKEN_NOT_ACCEPT);
            }

            // 2、判断Token是否过期
            Map map = JwtUtils.unSign(token);
            if(map == null) {
                return ResultVOUtils.error(ResultEnum.TOKEN_EXPIRE);
            }

            return ResultVOUtils.success(map);
        } catch (Exception e) {
            log.error("token缓存出现错误，错误位置：{}，错误栈：{}", "JwtServiceImpl.checkToken()", HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.TOKEN_READ_ERROR);
        }
    }

    @Override
    public ResultVO deleteToken(String username) {
        try {
            jedisClient.hdel(key, username);

            return ResultVOUtils.success();
        } catch (Exception e) {
            log.error("token缓存出现错误，错误位置：{}，错误栈：{}", "JwtServiceImpl.deleteToken()", HttpClientUtils.getStackTraceAsString(e));
            return ResultVOUtils.error(ResultEnum.TOKEN_READ_ERROR);
        }
    }
}
