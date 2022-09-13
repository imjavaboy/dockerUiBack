package com.gbq.docker.uiproject.commons.websocket;


import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.SpringBeanFactoryUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 郭本琪
 * @description websocket服务器
 * @date 2022/9/11 18:33
 * @Copyright 总有一天，会见到成功
 */
@ServerEndpoint("/ws/{userId}")
@Component
@Slf4j
public class WebSocketServer {
    @Resource
    private JedisClient jedisClient;

    private static HashMap<String, Session> webSocketSet = new HashMap<>();

    private final String key = "session";

    private final String ID_PREFIX = "UID:";

    private Session session;

    FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId){
        if (jedisClient == null) {
            jedisClient = SpringBeanFactoryUtils.getBean(JedisClient.class);
        }
        this.session = session;
        webSocketSet.put(session.getId(),session);
        String field = ID_PREFIX + userId;

        try {
            String res = jedisClient.hget(key, field);
            if (StringUtils.isNotBlank(res)) {
                jedisClient.hdel(key,field);
            }
            log.info("WebSocket连接建立成功，用户ID：{}", userId);
        } catch (Exception e) {
            log.error("缓存读取异常，错误位置：{}", "WebSocketServer.onOpen()");
        }
        try {
            jedisClient.hset(key, field, session.getId());
        } catch (Exception e) {
            log.error("缓存存储异常，错误位置：{}", "WebSocketServer.onOpen()");
        }
    }


    @OnClose
    public void onClose(@PathParam("userId") String userId){
        webSocketSet.remove(this.session.getId());
        String field = ID_PREFIX + userId;
        try {
            String res = jedisClient.hget(key, field);
            if (StringUtils.isNotBlank(res)) {
                //如果有，则删除原来的sessionId
                jedisClient.hdel(key, field);
            }
//            log.info("WebSocket连接关闭，用户ID：{}", userId);
        } catch (Exception e) {
            log.error("缓存读取异常，错误位置：{}", "WebSocketServer.OnClose()");
        }
    }
    @OnError
    public void onError(Throwable error) {
        System.out.println("WebSocket连接出错");
    }


    @OnMessage
    public void onMessage(String message){
        try {
            Map<String,String> map = new HashMap<>();
            map.put("info","heart");
            sendMessage(JsonUtils.objectToJson(map),this.session.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  发送消息方式
     * @param
     * @since 2022/9/11
     * @return
     */
    public void sendMessage(String message,String sessionId) throws IOException {
        Session session = webSocketSet.get(sessionId);
        if (session != null) {
            session.getBasicRemote().sendText(message);
        }else{
            throw new IOException("webSocket连接中断");
        }
    }

}
