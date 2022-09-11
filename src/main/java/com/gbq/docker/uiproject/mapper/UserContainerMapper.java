package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.gbq.docker.uiproject.domain.entity.UserContainer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 郭本琪
 * @description 用户容器表
 * @date 2022/9/9 9:12
 * @Copyright 总有一天，会见到成功
 */
public interface UserContainerMapper extends BaseMapper<UserContainer> {

    /**
     *  统计用户容器数量
     * @param
     * @since 2022/9/9
     * @return
     */
    Integer countByUserId(@Param("userId") String userId,  @Param("status") Integer status);

    /**
     *  获取所有容器
     * @param
     * @since 2022/9/9
     * @return
     */
    List<UserContainer> listContainerByUserIdAndNameAndStatus(Page<UserContainer> page, @Param("uid") String uid, @Param("name") String name, @Param("status") Integer status);
}
