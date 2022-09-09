package com.gbq.docker.uiproject.commons.activemq;


import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.exception.JsonException;
import com.gbq.docker.uiproject.service.SysLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

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
}
