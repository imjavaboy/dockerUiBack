package com.gbq.docker.uiproject.domain.vo;

import lombok.Data;

/**
 * @author gbq
 * @since 2022/9/10 21:27
 */
@Data
public class UserVO {
    private String userId;

    private String username;

    private String email;

    private Integer roleId;
}
