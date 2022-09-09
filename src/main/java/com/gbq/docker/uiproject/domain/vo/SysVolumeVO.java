package com.gbq.docker.uiproject.domain.vo;

import com.gbq.docker.uiproject.domain.entity.SysVolume;
import com.spotify.docker.client.messages.Volume;

import lombok.Data;

/**
 *
 * @author gbq
 * @since 2022/9/10 22:19
 */
@Data
public class SysVolumeVO extends SysVolume {
    private String typeName;

    private String objName;

    private Volume volume;
}
