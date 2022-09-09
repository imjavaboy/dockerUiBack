package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.entity.SysLog;
import com.gbq.docker.uiproject.domain.enums.SysLogTypeEnum;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 郭本琪
 * @description 日志服务
 * @date 2022/9/9 12:57
 * @Copyright 总有一天，会见到成功
 */
public interface SysLogService extends IService<SysLog> {

    /**
     *  保存日志
     * @param
     * @since 2022/9/9
     * @return
     */
    void saveLog(HttpServletRequest request, SysLogTypeEnum enums, Exception ex);
}
