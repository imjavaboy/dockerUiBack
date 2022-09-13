package com.gbq.docker.uiproject.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.gbq.docker.uiproject.domain.dto.NoticeDTO;
import com.gbq.docker.uiproject.domain.entity.UserNotice;
import com.gbq.docker.uiproject.domain.entity.UserNoticeDesc;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 21:28
 * @Copyright 总有一天，会见到成功
 */
public interface NoticeMapper extends BaseMapper<NoticeDTO> {

    /**
     *  保存消息
     * @param
     * @since 2022/9/11
     * @return
     */
    void saveNotice(@Param("notice") UserNotice notice);

    /**
     *  保存消息详情
     * @param
     * @since 2022/9/11
     * @return
     */
    void saveNoticeDesc(@Param("noticeDesc") UserNoticeDesc noticeDesc);

    /**
     *  获取个人所有通知
     * @param
     * @since 2022/9/11
     * @return
     */
    List<NoticeDTO> listUserNotice(@Param("userId") String userId, @Param("type") Integer type, Pagination page);

    /**
     *  将消息标记已读
     * @param
     * @since 2022/9/12
     * @return
     */
    void readNotice(@Param("ids") String[] ids, @Param("userId") String userId);


    /**
     *  未读消息的集合
     * @param
     * @since 2022/9/12
     * @return
     */
    List<String> listUnReadIds(@Param("uid") String uid, @Param("type") Integer type);

    /**
     *  批量删除通知
     * @param
     * @since 2022/9/12
     * @return
     */
    void deleteNotice(@Param("ids") String[] idArray, @Param("userId") String userId);

    /**
     *  统计未读数量
     * @param
     * @since 2022/9/12
     * @return
     */
    Integer countUnread(String uid);
}
