package com.gbq.docker.uiproject.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DockerHub注册信息
 * @author gbq
 * @since 2022/9/7 21:31
 */
@Data
public class DockerHubRegister implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "DockerHub用户名不能为空")
    private String username;

    @NotNull(message = "DockerHub密码不能为空")
    private String password;
}
