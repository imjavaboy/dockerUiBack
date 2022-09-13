package com.gbq.docker.uiproject.commons.schedule;


import com.gbq.docker.uiproject.service.RepositoryImageService;
import com.gbq.docker.uiproject.service.SysImageService;
import com.gbq.docker.uiproject.service.UserContainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 郭本琪
 * @description 数据同步定时任务
 * @date 2022/9/12 20:17
 * @Copyright 总有一天，会见到成功
 */
@Component
public class DataSyncSchedule {
    @Resource
    private UserContainerService containerService;
    @Resource
    private SysImageService sysImageService;
    @Resource
    private RepositoryImageService repositoryImageService;



    /**
     *  同步容器到数据库
     * @param
     * @since 2022/9/12
     * @return
     */
    @Scheduled(initialDelay=1000, fixedRate=900_000)
    public void syncContainers() {
        containerService.sync();
    }
    /**
     *  同步容器的状态，15分种一次
     * @param
     * @since 2022/9/12
     * @return
     */
    @Scheduled(initialDelay=1000, fixedRate=900_000)
    public void syncContainerStatus() {
        containerService.syncStatus(null);
    }

    /**
     *  同步系统镜像，15分种一次

     * @since 2022/9/12
     * @return
     */
    @Scheduled(initialDelay=1000, fixedRate=900_000)
    public void syncSystemImage() {
        sysImageService.sync();
    }

    /**
     *  同步Hub镜像
     * @param
     * @since 2022/9/12
     * @return
     */

    @Scheduled(initialDelay=1000, fixedRate=900_000)
    public void syncHubImage() {
        repositoryImageService.sync();
    }
}
