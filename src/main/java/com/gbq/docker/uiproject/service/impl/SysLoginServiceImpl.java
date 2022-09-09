package com.gbq.docker.uiproject.service.impl;



import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.gbq.docker.uiproject.commons.activemq.MQProducer;
import com.gbq.docker.uiproject.commons.activemq.Task;
import com.gbq.docker.uiproject.commons.util.*;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.entity.SysLog;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.exception.CustomException;
import com.gbq.docker.uiproject.exception.JsonException;
import com.gbq.docker.uiproject.mapper.SysLoginMapper;
import com.gbq.docker.uiproject.service.SysLoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/7 15:41
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class SysLoginServiceImpl extends ServiceImpl<SysLoginMapper,SysLogin> implements SysLoginService {

    private final String USERNAME_PREFIX = "NAME:";
    private final String ID_PREFIX = "ID:";
    private final String EMAIL_PREFIX = "EMAIL:";


    @Value("${redis.login.key}")
    private String key;
    @Value("${redis.register.email.expire}")
    private int registerEmailExpire;
    @Value("${redis.register.email.key}")
    private String registerEmailKey;
    @Value("${spring.mail.username}")
    private String senderAddress;
    @Value("${server.addr}")
    private String serverIp;



    @Resource
    private JedisClient jedisClient;
    @Resource
    private SysLoginMapper loginMapper;
    @Resource
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;
    @Resource
    private MQProducer mqProducer;

    @Override
    public SysLogin getById(String id) {
        String filed = ID_PREFIX+id;
        try {
            String res = jedisClient.hget(key, filed);
            if (StringUtils.isNotBlank(res)) {
                return JsonUtils.jsonToObject(res,SysLogin.class);
            }
        } catch (JsonException e) {
            log.error("缓存读取异常或json转换异常，错误位置：SysLoginServiceImpl.getById()");
        }
        SysLogin sysLogin = loginMapper.selectById(id);
        if (sysLogin == null){
            return null;
        }

        try {
            jedisClient.hset(key,filed,JsonUtils.objectToJson(sysLogin));
        } catch (Exception e) {
            log.error("缓存村粗异常，异常位置，SysLoginServiceImpl.getById()");
        }
        return sysLogin;
    }

    @Override
    public SysLogin getByUserName(String username) {
        if (StringUtils.isBlank(username)){
            return null;
        }
        String filed = USERNAME_PREFIX + username;
        //先去缓存获取

        try {
            String res = jedisClient.hget(key, filed);
            if (StringUtils.isNotBlank(res)){
                return JsonUtils.jsonToObject(res,SysLogin.class);
            }
        } catch (JsonException e) {
            log.error("缓存读取异常或者json转换异常，错误位置：SysLoginServiceImpl.getByUsername()");
        }
        List<SysLogin> list = loginMapper.selectList(new EntityWrapper<SysLogin>().eq("username", username));

        SysLogin listFirst = CollectionUtils.getListFirst(list);
        if (listFirst == null){
            return null;
        }
        try {
            jedisClient.hset(key,filed,JsonUtils.objectToJson(listFirst));
        } catch (Exception e) {
            log.error("缓存存储异常，错误位置：SysLoginServiceImpl.getByUsername()");
        }
        return listFirst;
    }

    @Override
    public boolean checkPassword(String username, String password) {
        SysLogin sysLogin = getByUserName(username);
        if (sysLogin == null) {
            return  false;
        }

        return new BCryptPasswordEncoder().matches(password,sysLogin.getPassword());
    }



    @Override
    public boolean hasFreeze(String username) {
        SysLogin login = getByUserName(username);

        if(login != null) {
            return login.getHasFreeze();
        }
        return false;
    }

    @Override
    public ResultVO registerCheck(String username, String email) {
        if (StringUtils.isBlank(username,email)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        if(getByUserName(username) != null) {
            return ResultVOUtils.error(ResultEnum.REGISTER_USERNAME_ERROR);
        }
        // 校验邮箱
        if(!StringUtils.isEmail(email)) {
            return ResultVOUtils.error(ResultEnum.EMAIL_DIS_LEGAL);
        }
        if(getByEmail(email) != null) {
            return ResultVOUtils.error(ResultEnum.REGISTER_EMAIL_ERROR);
        }
        return ResultVOUtils.success();
    }

    @Override
    public SysLogin getByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        String filed = EMAIL_PREFIX + email;

        try {
            String res = jedisClient.hget(key, filed);
            if (StringUtils.isNotBlank(res)) {
                    return JsonUtils.jsonToObject(res, SysLogin.class);
            }
        } catch (JsonException e) {
            log.error("缓存读取异常或者json转换移仓，错误位置：SysLoginServiceImpl.getByEmail()");
        }
        List<SysLogin> loginList = loginMapper.selectList(new EntityWrapper<SysLogin>().eq("email", email));
        SysLogin listFirst = CollectionUtils.getListFirst(loginList);
        if (listFirst == null) {
            return null;
        }
        try {
            jedisClient.hset(key, filed, JsonUtils.objectToJson(listFirst));
        } catch (Exception e) {
            log.error("缓存存储异常，错误位置：SysLoginServiceImpl.getByEmail()");
        }

        return listFirst;
    }

    @Override
    public boolean save(SysLogin sysLogin) {

        if (StringUtils.isNotBlank(sysLogin.getPassword())) {
            sysLogin.setPassword(new BCryptPasswordEncoder().encode(sysLogin.getPassword()));
        }
        sysLogin.setRoleId(RoleEnum.ROLE_USER.getCode());
        Integer insert = loginMapper.insert(sysLogin);
        return insert == 1;
    }


    /**
     *  发送注册邮件
     * @param
     * @since 2022/9/8
     * @return
     */
    @Override
    public Boolean sendRegisterEmial(String email) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;

        // 生成token，注意jwt过期时间为ms
        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("timestamp", System.currentTimeMillis());
        String token = JwtUtils.sign(map, registerEmailExpire * 1000);
        try {
            // 将token放入缓存
            jedisClient.sadd(registerEmailKey, token);
        } catch (Exception e) {
            log.error("缓存存储异常，错误位置：SysLoginServiceImpl.sendRegisterEmail()");
            // 因为邮箱注册依赖redis，因此redis失效不允许注册
            return false;
        }
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(senderAddress);
            helper.setTo(email);
            helper.setSubject("欢迎注册【DockerUI系统】");

            Context context = new Context();
            // 去除token前缀
            Map<String,Object> vars = new HashMap<>();
            vars.put("registerUrl",token.substring(7));
            vars.put("serverIp",serverIp);
            //context.setVariable("registerUrl", token.substring(7));
            context.setVariables(vars);
            String emailContent = templateEngine.process("mail", context);
            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);

            // 发送延时消息
            Map<String,String> maps = new HashMap<>(16);
            maps.put("email",email);
            Task task = new Task("邮箱注册任务", maps);
            Destination destination = new ActiveMQQueue("MQ_QUEUE_REGISTER");

//
            mqProducer.delaySend(destination, JsonUtils.objectToJson(task), (long)registerEmailExpire * 1000);

            return true;
        } catch (MessagingException e) {
            log.error("发送邮件异常，错误位置：SysLoginServiceImpl.sendEmail()，目标邮箱：{}", email);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = CustomException.class)
    public Boolean verifyRegisterEmail(String token) {
        String email;
        //检验token失效
        try {

            token = "Bearer "+token;

            Boolean isTokenExist = jedisClient.sismember(registerEmailKey, token);
            if (!isTokenExist){
                return false;
            }
            Map map = JwtUtils.unSign(token);

            long timestamp = (long) map.get("timestamp");
            long now = System.currentTimeMillis();
            if (registerEmailExpire < (now-timestamp)/1000){
                return false;
            }
            email = (String) map.get("email");
        } catch (Exception e) {
            log.error("缓存读取异常，或验证邮箱token错误，错误位置：SysLoginServiceImpl.verifyRegisterEmail()");
            return false;
        }

        //解冻
        SysLogin loginUser = getByEmail(email);
        if (null != loginUser){
            loginUser.setHasFreeze(false);
            Integer integer = loginMapper.updateById(loginUser);
            if (integer != 1){
                jedisClient.srem(registerEmailKey, token);
                cleanLoginCache(loginUser);
                return false;
            }
        }else{
            log.error("验证邮箱用户不存在，错误位置：SysLoginServiceImpl.verifyRegisterEmail()，目标email：", email);
            return false;
        }

        //删除token
        try {
            jedisClient.srem(registerEmailKey, token);
            cleanLoginCache(loginUser);
        } catch (Exception e) {
            log.error("缓存存储异常，错误位置：SysLoginServiceImpl.verifyRegisterEmail()");
        }

        return true;
    }

    @Override
    public void cleanLoginCache(SysLogin login) {
        try {
            if (StringUtils.isNotBlank(login.getId())) {
                jedisClient.hdel(key, ID_PREFIX + login.getId());
            }
            if (StringUtils.isNotBlank(login.getUsername())) {
                jedisClient.hdel(key, USERNAME_PREFIX + login.getUsername());
            }
            if (StringUtils.isNotBlank(login.getEmail())) {

                jedisClient.hdel(key, EMAIL_PREFIX + login.getEmail());
            }
        } catch (Exception e) {
            log.error("缓存删除异常，错误位置：SysLoginServiceImpl.cleanLoginCache()");
        }
    }
}
