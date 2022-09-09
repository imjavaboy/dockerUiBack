package com.gbq.docker.uiproject.filter;


import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.ResultVOUtils;
import com.gbq.docker.uiproject.commons.util.SpringBeanFactoryUtils;
import com.gbq.docker.uiproject.commons.util.StringUtils;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.vo.UserVO;
import com.gbq.docker.uiproject.service.JwtService;
import com.gbq.docker.uiproject.service.SysLoginService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * @author 郭本琪
 * @description 验证用户名密码并生成token
 * @date 2022/9/8 11:45
 * @Copyright 总有一天，会见到成功
 */

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;

    public JwtLoginFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     *  验证后调用，将用户信息从request取出
     * @param
     * @since 2022/9/8
     * @return
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String username = ((User)authResult.getPrincipal()).getUsername();

        // 判断用户是否被冻结
        SysLoginService loginService = SpringBeanFactoryUtils.getBean(SysLoginService.class);
        if(loginService.hasFreeze(username)) {
            request.setAttribute("ERR_MSG", ResultEnum.LOGIN_FREEZE);
            request.getRequestDispatcher("/auth/error").forward(request,response);
        }else{

            // 生成Token
            JwtService jwtService = SpringBeanFactoryUtils.getBean(JwtService.class);
            String token = jwtService.genToken(username);
            response.reset();
            // 将token放入响应头中
            response.addHeader("Authorization", token);
            response.setHeader("Access-Control-Expose-Headers","Authorization");

            response.setContentType("text/html;charset=utf-8");
            // 读取用户信息
            UserVO userVO = jwtService.getUserInfo(token);
            String json = JsonUtils.objectToJson(ResultVOUtils.success(userVO));

            response.getWriter().write(json);
        }
    }

    /**
     *  验证前调用，将用户信息从request取出
     * @param
     * @since 2022/9/8
     * @return
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (StringUtils.isBlank(username,password)) {
            return null;
        }
        password = getEncryPassword(username, password);

        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password,
                            Collections.emptyList())
            );
        } catch (AuthenticationException e) {
            try {
                request.setAttribute("ERR_MSG", ResultEnum.LOGIN_ERROR);
                request.getRequestDispatcher("/auth/error").forward(request, response);
            } catch (Exception e1) {
               logger.info("异常，JwtLoginFilter的attemptAuthentication()");
            }
        }
        return null;
    }


    /**
     *  获取加密的密码
     *  如果用户身份验证失败，返回原密码
     * @param
     * @since 2022/9/8
     * @return
     */
    private String getEncryPassword(String username,String password){
        SysLoginService loginService = SpringBeanFactoryUtils.getBean(SysLoginService.class);

        if(loginService.checkPassword(username,password)){
            SysLogin sysLogin = loginService.getByUserName(username);
            return sysLogin.getPassword();
        }else{
            return password;
        }
    }
}
