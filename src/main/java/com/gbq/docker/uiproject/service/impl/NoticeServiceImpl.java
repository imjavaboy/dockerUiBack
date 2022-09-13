package com.gbq.docker.uiproject.service.impl;


import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.activemq.MQProducer;
import com.gbq.docker.uiproject.commons.activemq.Task;
import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.RandomUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.domain.dto.NoticeDTO;
import com.gbq.docker.uiproject.domain.entity.UserNotice;
import com.gbq.docker.uiproject.domain.entity.UserNoticeDesc;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.WebSocketTypeEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.mapper.NoticeMapper;
import com.gbq.docker.uiproject.mapper.SysLoginMapper;
import com.gbq.docker.uiproject.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.Destination;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/11 21:27
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, NoticeDTO> implements NoticeService {


    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private SysLoginMapper loginMapper;
    @Resource
    private MQProducer mqProducer;

    @Async("taskExecutor")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendUserTask(String title, String content, Integer type, boolean sendAll, List<String> receivers, String sender) {

        try {
            UserNotice notice = new UserNotice();
            String noticeId = RandomUtils.time();
            notice.setId(noticeId);
            notice.setTitle(title);
            notice.setContent(content);
            notice.setType(type);
            notice.setSender(sender);
            notice.setHasAll(sendAll);
            noticeMapper.saveNotice(notice);

            if(sendAll) {
                receivers = loginMapper.listId();
            }
            int total = receivers.size(), count = 0;
            for(String id : receivers) {
                if(loginMapper.hasExist(id)) {
                    UserNoticeDesc noticeDesc = new UserNoticeDesc(RandomUtils.uuid(), noticeId, id, false);
                    noticeMapper.saveNoticeDesc(noticeDesc);
                    count++;
                }
            }
            String msg = "通知发送成功，预计发送数：" + total + "，实际发送数：" + count;
            sendMQ(sender, ResultVOUtils.successWithMsg(msg));
        } catch (Exception e) {
            log.error("通知发送失败，错误位置：{}，错误栈：{}",
                    "NoticeServiceImpl.sendUserTask()", HttpClientUtils.getStackTraceAsString(e));
            sendMQ(sender, ResultVOUtils.successWithMsg("通知发送失败"));
        }
    }

    /**
     *  发送通知消息
     * @param
     * @since 2022/9/11
     * @return
     */
    private void sendMQ(String sender, ResultVO successWithMsg) {
        Destination destination = new ActiveMQQueue("MQ_QUEUE_NOTICE");
        Task task = new Task();

        Map<String, Object> data = new HashMap<>(16);
        data.put("type", WebSocketTypeEnum.NOTICE.getCode());
        successWithMsg.setData(data);

        Map<String,String> map = new HashMap<>(16);
        map.put("uid",sender);
        map.put("data", JsonUtils.objectToJson(successWithMsg));
        task.setData(map);

        mqProducer.send(destination, JsonUtils.objectToJson(task));

    }

    @Override
    public Page<NoticeDTO> listSelfNotice(String uid, Integer type, Page<NoticeDTO> page) {
        List<NoticeDTO> noticeDTOS = noticeMapper.listUserNotice(uid, type, page);

        return page.setRecords(noticeDTOS);
    }

    @Override
    public ResultVO readNotice(String[] ids, String uid) {
        try {
            noticeMapper.readNotice(ids, uid);
            return ResultVOUtils.success();
        } catch (Exception e) {
            return ResultVOUtils.error(ResultEnum.NOTICE_READ_ERROR);
        }
    }

    @Override
    public ResultVO readAllNotice(String uid, Integer type) {
        try {
            List<String> ids = noticeMapper.listUnReadIds(uid, type);
            noticeMapper.readNotice(ids.toArray(new String[ids.size()]), uid);
            return ResultVOUtils.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVOUtils.error(ResultEnum.NOTICE_READ_ERROR);
        }
    }

    @Override
    public ResultVO deleteNotice(String[] idArray, String uid) {
        noticeMapper.deleteNotice(idArray, uid);

        return ResultVOUtils.success();
    }

    @Override
    public Integer countUnread(String uid) {
        return noticeMapper.countUnread(uid);
    }
}
