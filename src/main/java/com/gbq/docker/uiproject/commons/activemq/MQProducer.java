package com.gbq.docker.uiproject.commons.activemq;


import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.Serializable;

/**
 * @author 郭本琪
 * @description ActiceMq的生产者
 * @date 2022/9/8 18:08
 * @Copyright 总有一天，会见到成功
 */
@Component
@Slf4j
public class MQProducer {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    /**
     *  即时发送
     * @param
     * @since 2022/9/8
     * @return
     */
    public void send(Destination destination, String message){
        jmsMessagingTemplate.convertAndSend(destination,message);
    }

    /**
     *  延时发送
     * @param
     * @since 2022/9/8
     * @return
     */

    public <T extends Serializable> void delaySend(Destination destination, T data, Long time) {

        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        // 获取连接工厂
        ConnectionFactory connectionFactory = jmsMessagingTemplate.getConnectionFactory();
        try {
            // 获取连接
            connection = connectionFactory.createConnection();
            connection.start();
            // 获取session，true开启事务，false关闭事务
            session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
            // 创建一个消息队列
            producer = session.createProducer(destination);
            producer.setDeliveryMode(JmsProperties.DeliveryMode.PERSISTENT.getValue());
            ObjectMessage message = session.createObjectMessage(data);
            //设置延迟时间
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, time);
            // 发送消息
            producer.send(message);
            log.info("发送消息：{},time,{}", data,time);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (producer != null) {
                    producer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
       /** 得到jmsTemplate
        JmsTemplate jmsTemplate = jmsMessagingTemplate.getJmsTemplate();
        // 发送消息
        jmsTemplate.send(destination, session -> {
            TextMessage textMessage = session.createTextMessage(message);
            // 设置延时时间
            System.out.println("设置延迟时间："+time);
            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, time);
            return textMessage;
        });

        */

}
