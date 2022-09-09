package com.gbq.docker.uiproject.domain.vo;


import com.gbq.docker.uiproject.domain.entity.ContainerNetwork;
import com.gbq.docker.uiproject.domain.entity.SysNetwork;
import lombok.Data;

@Data
public class ContainerNetworkVO extends ContainerNetwork {
    private SysNetwork network;
}
