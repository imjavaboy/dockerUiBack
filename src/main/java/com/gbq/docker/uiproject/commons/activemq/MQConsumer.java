package com.gbq.docker.uiproject.commons.activemq;


import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.commons.websocket.WebSocketServer;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.exception.JsonException;
import com.gbq.docker.uiproject.service.SysLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author 郭本琪
 * @description mq消费
 * @date 2022/9/8 18:23
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
@Component
public class MQConsumer {
    @Resource
    private SysLoginService loginService;
    @Resource
    private JedisClient jedisClient;
    @Resource
    private WebSocketServer webSocketServer;



    private final String key = "session" ;
    private final String ID_PREFIX = "UID:";

    /**
     *  消费注册邮箱的消息
     * @param
     * @since 2022/9/11
     * @return
     */
    @JmsListener(destination = "MQ_QUEUE_REGISTER")
    public void recevieRegister(String text){
        if (StringUtils.isNotBlank(text)) {
            try {
                Task task = JsonUtils.jsonToObject(text, Task.class);
                Map<String, String> data = task.getData();
                String email = data.get("email");
                log.info("验证未激活邮箱，目标邮箱：{}", email);
                SysLogin login = loginService.getByEmail(email);
                log.info("是否冻结？+{}",login.getHasFreeze());
                if(login != null && login.getHasFreeze()) {
                    loginService.deleteById(login);
                    loginService.cleanLoginCache(login);
                }
            } catch (JsonException e) {
               log.info("有错误，在MQConsumer的recevieRegister，c错误信息{}",e.getMessage());
            }
        }
    }

    /**
     *  消费系统镜像相关的消息
     * @param
     * @since 2022/9/11
     * @return
     */
    @JmsListener(destination = "MQ_QUEUE_SYS_IMAGE")
    public void receiveSysImage(String text) {
        if (StringUtils.isNotBlank(text)) {
            Task task = null;
            try {
                task = JsonUtils.jsonToObject(text, Task.class);
            } catch (JsonException e) {
               log.error("json转换异常，MQConsumer.receiveSysImage()");
            }

            Map<String, String> data = task.getData();
            String uid = data.get("uid");
            String field = ID_PREFIX + uid;

            try {
                String sessionId = jedisClient.hget(key, field);
                if (StringUtils.isNotBlank(sessionId)) {
                    webSocketServer.sendMessage(data.get("data"),sessionId);
                }else{
                    throw new Exception("session未找到");
                }
            } catch (Exception e) {
                log.error("接收系统镜像消息错误，错误位置：{}，错误信息：{}", "MQConsumer.receiveSysImage()", e.getMessage());

            }

        }
    }
    /**
     *  接收通知消息
     * @param
     * @since 2022/9/11
     * @return
     */
    @JmsListener(destination = "MQ_QUEUE_NOTICE")
    public void receiveNotice(String text) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(text)) {
            String userId = null;
            ResultVO resultVO = null;
            try {
                Task task = JsonUtils.jsonToObject(text, Task.class);

                Map<String, String> map = task.getData();
                userId = map.get("uid");
                resultVO = JsonUtils.jsonToObject(map.get("data"), ResultVO.class);
            } catch (JsonException e) {
                log.error("json转换异常,位置MQConsumer.receiveService()");
            }

            String field = ID_PREFIX + userId;
            try {
                String sessionId = jedisClient.hget(key, field);
                if (org.apache.commons.lang3.StringUtils.isNotBlank(sessionId)) {
                    webSocketServer.sendMessage(JsonUtils.objectToJson(resultVO), sessionId);
                } else {
                    throw new Exception("session未找到");
                }
            } catch (Exception e) {
                log.error("接收通知消息错误，错误位置：{}，错误信息：{}", "MQConsumer.receiveService()", e.getMessage());
            }
        }
    }

    @JmsListener(destination = "MQ_QUEUE_CONTAINER")
    public void receiveContainer(String text) {
        if (StringUtils.isNotBlank(text)) {
            Task task = null;
            try {
                task = JsonUtils.jsonToObject(text, Task.class);
            } catch (JsonException e) {
               log.error("json转换异常：MQConsumer.receiveContainer()");
            }

            Map<String, String> map = task.getData();
            String userId = map.get("uid");

            String field = ID_PREFIX + userId;

            try {
                String sessionId = jedisClient.hget(key, field);
                if (StringUtils.isNotBlank(sessionId)) {
                    webSocketServer.sendMessage(map.get("data"), sessionId);
                } else {
                    throw new Exception("session未找到");
                }
            } catch (Exception e) {
                log.error("接收容器消息错误，错误位置：{}，错误信息：{}", "MQConsumer.receiveContainer()", e.getMessage());
            }
        }
    }
}
