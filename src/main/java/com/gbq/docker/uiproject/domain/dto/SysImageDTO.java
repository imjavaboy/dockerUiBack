package com.gbq.docker.uiproject.domain.dto;


import com.gbq.docker.uiproject.domain.entity.SysImage;
import lombok.Data;

@Data
public class SysImageDTO extends SysImage {
    /**
     * 所属用户名
     */
    private String username;
}
