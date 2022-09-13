package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.domain.entity.SysLog;
import com.gbq.docker.uiproject.domain.enums.SysLogTypeEnum;
import com.gbq.docker.uiproject.mapper.SysLogMapper;
import com.gbq.docker.uiproject.service.SysLogService;
import com.gbq.docker.uiproject.service.SysLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 13:00
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class SysLogServiceImpl  extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {
    @Resource
    private SysLogMapper sysLogMapper;

    @Override
    public void saveLog(HttpServletRequest request, SysLogTypeEnum enums, Exception ex) {
        SysLog log = new SysLog();
        log.setParam(request.getParameterMap());
        System.out.println("十九届世界是");
        request.getParameterMap().forEach((k,v)->{
            System.out.print(k+":::");
            for (String s : v) {
                System.out.print(s+",");
            }
        });
        log.setType(enums.getCode());
        log.setDescription(enums.getMessage());
        log.setUserId((String)request.getAttribute("uid"));
        log.setAction(request.getRequestURI());
        log.setMethod(request.getMethod());
        log.setIp(HttpClientUtils.getRemoteAddr(request));
        log.setUserAgent(request.getHeader("user-agent"));

        if(ex != null) {
            log.setException(ex.getMessage().substring(0,255));
        }

        sysLogMapper.insert(log);
    }
}
