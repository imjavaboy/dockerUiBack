package com.gbq.docker.uiproject.service;


import com.gbq.docker.uiproject.domain.vo.ResultVO;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/9 9:06
 * @Copyright 总有一天，会见到成功
 */

public interface MonitorService {


    /**
     *  读取用户docker的信息
     * @param
     * @since 2022/9/9
     * @return
     */
    ResultVO getUserDockerInfo(String uid);

    /**
     *  获取docker宿主机信息
     * @param
     * @since 2022/9/9
     * @return
     */
    ResultVO getDockerInfo();
}
