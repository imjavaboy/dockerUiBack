package com.gbq.docker.uiproject.domain.dto;

import com.gbq.docker.uiproject.domain.entity.UserService;
import lombok.Data;

/**
 * 容器DTO
 * @author gbq
 * @since 2022/9/7 09:10
 */
@Data
public class UserServiceDTO extends UserService {
    /**
     * 所属项目名
     */
    private String projectName;

    /**
     * 状态名
     */
    private String statusName;

    /**
     * IP
     */
    private String ip;
}
