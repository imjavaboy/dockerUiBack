package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.entity.RepositoryImage;
import com.gbq.docker.uiproject.domain.entity.SysImage;
import com.gbq.docker.uiproject.domain.vo.HubImageVO;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 12:10
 * @Copyright 总有一天，会见到成功
 */

public interface RepositoryImageService extends IService<RepositoryImage> {

    /**
     *  获取镜像列表
     * @param
     * @since 2022/9/11
     * @return
     */
    List<HubImageVO> listHubImageVO();

    /**
     *  同步hub和数据库的信息
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO sync();


    /**
     *  列出hub所有的仓库
     * @param
     * @since 2022/9/12
     * @return
     */
    List<String> listRepositoryFromHub() throws Exception ;


    /**
     *  列出指定镜像的tag
     * @param
     * @since 2022/9/12
     * @return
     */
    List<String> listTagsFromHub(String fullName) throws Exception ;

    /**
     *  查询镜像的digest
     * @param
     * @since 2022/9/12
     * @return
     */
    String getDigest(String name, String tag) throws Exception ;

    /**
     *  Push前校验
     * @param
     * @since 2022/9/13
     * @return
     */
    ResultVO pushCheck(String imageId, String uid);

    Boolean hasExist(String fullName);


    /**
     *  * 上传本地镜像到Hub中
     * （1）只有普通用户能上传
      * （2）镜像类型属于私有且属于该用户才能上传
     * @param
     * @since 2022/9/13
     * @return
     */
    void pushTask(SysImage sysImage, String uid, HttpServletRequest request);

    /**
     * 从hub获取镜像
     * @param
     * @since 2022/9/13
     * @return
     */
    List<RepositoryImage> listByName(String name);
}
