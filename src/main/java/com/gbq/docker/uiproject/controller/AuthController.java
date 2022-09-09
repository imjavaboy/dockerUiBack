package com.gbq.docker.uiproject.controller;


import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.SysLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 郭本琪
 * @description 权限注册相关
 * @date 2022/9/8 14:55
 * @Copyright 总有一天，会见到成功
 */
@RestController
@RequestMapping("/auth")
@Slf4j
@Api(tags = "授权模块")
public class AuthController {


    @Value("${server.addr}")
    private String serverAddress;


    @Resource
    private SysLoginService loginService;

    @ApiOperation("用户注册校验用户名和密码")
    @PostMapping("/register/check")
    public ResultVO checkRegister(String username,String email){
        return loginService.registerCheck(username,email);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public ResultVO register(String username, String password, String email) {
        if(StringUtils.isBlank(username,password,email)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }

        ResultVO resultVO = loginService.registerCheck(username, email);
        if (ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }
        SysLogin sysLogin = new SysLogin(username, password, email);
        sysLogin.setHasFreeze(true);
        loginService.save(sysLogin);
        Boolean b =  loginService.sendRegisterEmial(email);
        return b ? ResultVOUtils.success("已经发送邮件验证") : ResultVOUtils.error(ResultEnum.EMAIL_SEND_ERROR);
    }

    @ApiOperation("验证邮件")
    @GetMapping("/email")
    public void email(String token, HttpServletResponse response){
        Boolean b = loginService.verifyRegisterEmail(token);
        String subject = b ? "注册成功" : "注册失败";
        String content = b ? "欢迎注册DockerUI平台，点击此处进入" : "用户已注册或邮件验证已过期，请重新注册";
        String imgUrl = "https://guobenqimall.oss-cn-beijing.aliyuncs.com/20190306134520829.jpg";
        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write("<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset='utf-8'>\n" +
                    "    <title></title>\n" +
                    "</head>\n" +
                    "<body background='"+imgUrl+"'>\n" +
                    "<div style='position: absolute; bottom:70%;left:50%;margin-left:-60px;'>\n" +
                    "    <h1>"+ subject +"</h1>\n" +
                    "</div>\n" +
                    "<div style='position: absolute; bottom:65%;left:45.5%;margin-left:-60px;'>\n" +
                    "    <a href='"+serverAddress+"'>" + content + "</a>\n" +
                    "</div>\n" +
                    "</body>\n" +
                    "</html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("异常路径")
    @RequestMapping("/error")
    public ResultVO loginError(HttpServletRequest request) {
        // 如果Spring Security中有异常，输出
        AuthenticationException exception =
                (AuthenticationException)request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        if(exception != null) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR.getCode(), exception.toString());
        }

        // 其次从ERR_MSG中取
        Object obj = request.getAttribute("ERR_MSG");
        if(obj instanceof ResultVO) {
            return (ResultVO)obj;
        }
        if(obj instanceof ResultEnum) {
            return ResultVOUtils.error((ResultEnum)obj);
        } else {
            return ResultVOUtils.error(ResultEnum.OTHER_ERROR);
        }
    }


}
