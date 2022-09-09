package com.gbq.docker.uiproject.domain.dto;

import com.gbq.docker.uiproject.domain.entity.SysLog;
import lombok.Data;

@Data
public class SysLogDTO extends SysLog {
    /**
     * 用户名
     */
    private String username;

    /**
     * 是否有异常
     */
    private Boolean hasException;
}
