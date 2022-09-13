package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.dto.NoticeDTO;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

import java.util.List;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 21:26
 * @Copyright 总有一天，会见到成功
 */
public interface NoticeService  extends IService<NoticeDTO> {

    /**
     *   发送消息给用户任务
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型，不能为空
     * @param sendAll 是否发送全体用户
     * @param receivers 接收用户ID数组，当发送全体用户时，参数无效
     * @since 2022/9/11
     * @return
     */
    void sendUserTask(String title, String content, Integer type, boolean sendAll, List<String> receivers, String sender);


    /**
     *获取个人所有的通知
     * @param type 消息类型，null查询所有
     * @since 2022/9/11
     * @return
     */
    Page<NoticeDTO> listSelfNotice(String uid, Integer type, Page<NoticeDTO> page);

    /**
     *  将消息标记为已读
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO readNotice(String[] ids, String uid);

    /**
     *  已读所有消息
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO readAllNotice(String uid, Integer type);

    /**
     *  删除消息
     * @param
     * @since 2022/9/12
     * @return
     */
    ResultVO deleteNotice(String[] idArray, String uid);

    /**
     *  统计未读消息数量
     * @param
     * @since 2022/9/12
     * @return
     */
    Integer countUnread(String uid);
}
