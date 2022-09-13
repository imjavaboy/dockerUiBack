package com.gbq.docker.uiproject.service;


/**
 * @author 郭本琪
 * @description
 * @date 2022/9/12 10:07
 * @Copyright 总有一天，会见到成功
 */

public interface PortService {

    /**
     *  端口是否被占用
     * @param
     * @since 2022/9/12
     * @return
     */
    boolean hasUse(Integer port);

    /**
     *  随机分配端口
     * @param
     * @since 2022/9/12
     * @return
     */
    Integer randomPort();
}
