package com.gbq.docker.uiproject;

import com.gbq.docker.uiproject.commons.activemq.Task;
import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.JwtUtils;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class UiprojectApplicationTests {

    @Resource
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    void contextLoads() {
    }

    @Test
    public void TestMail(){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;

        // 生成token，注意jwt过期时间为ms
        Map<String, Object> map = new HashMap<>();
        map.put("email","1159311344@qq.com");
        map.put("timestamp", System.currentTimeMillis());
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("1159311344@qq.com");
            helper.setTo("1159311344@qq.com");
            helper.setSubject("欢迎注册【DockerUI系统】");

            Context context = new Context();
            // 去除token前缀
            Map<String,Object> vars = new HashMap<>();
            vars.put("registerUrl","hsjsjsjkkdkdkdkddddddddddddd");
            vars.put("serverIp","http:127.0.0.1:9999");
            //context.setVariable("registerUrl", token.substring(7));
            context.setVariables(vars);
            String emailContent = templateEngine.process("mail", context);
            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);


        } catch (MessagingException e) {
            System.out.println(("发送邮件异常，错误位置：SysLoginServiceImpl.sendEmail()，目标邮箱：{}"));
        }
    }

}
