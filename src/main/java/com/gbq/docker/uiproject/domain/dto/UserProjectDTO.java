package com.gbq.docker.uiproject.domain.dto;

import com.gbq.docker.uiproject.domain.entity.UserProject;
import lombok.Data;

/**
 * 用户项目DTO
 * @author gbq
 * @since 2022/9/7 18:19
 */
@Data
public class UserProjectDTO extends UserProject {
    /**
     * 用户名
     */
    private String username;
}
