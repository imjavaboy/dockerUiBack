package com.gbq.docker.uiproject.filter;


import com.gbq.docker.uiproject.commons.util.HttpClientUtils;
import com.gbq.docker.uiproject.commons.util.JsonUtils;
import com.gbq.docker.uiproject.commons.util.SpringBeanFactoryUtils;
import com.gbq.docker.uiproject.commons.util.jedis.JedisClient;
import com.gbq.docker.uiproject.domain.enums.ResultEnum;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 郭本琪
 * @description jwt的过滤器
 * @date 2022/9/8 11:44
 * @Copyright 总有一天，会见到成功
 */
@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    private static final PathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!disProtectedUrl(request)) {
            Object obj = getAuthentication(request);

            if(obj instanceof ResultVO) {
                //如果属于ResultVO，表示有错误
                request.setAttribute("ERR_MSG", obj);
                // 转发到错误Url
                request.getRequestDispatcher("/auth/error").forward(request, response);
            } else if(obj instanceof UsernamePasswordAuthenticationToken) {
                SecurityContextHolder.getContext().setAuthentication((UsernamePasswordAuthenticationToken)obj);
               chain.doFilter(request, response);
            } else {
                // 如果验证失败，设置异常；否则将UsernamePasswordAuthenticationToken注入到框架中
                request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", new AuthenticationCredentialsNotFoundException("权限认证失败"));
                // 转发到错误Url
                request.getRequestDispatcher("/auth/error").forward(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
    /**
     * 验证token
     * @return 成功返回包含角色的UsernamePasswordAuthenticationToken；失败返回null
     */
    private Object getAuthentication(HttpServletRequest request) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String token = request.getHeader("Authorization");
        if (token == null) {
            return null;
        }
        JedisClient jedisClient = SpringBeanFactoryUtils.getBean(JedisClient.class);
        JwtService jwtService = SpringBeanFactoryUtils.getBean(JwtService.class);
        // 校验token
        ResultVO resultVO = jwtService.checkToken(token);
        if(ResultEnum.OK.getCode() != resultVO.getCode()){
            return resultVO;
        }

        Map map = (Map) resultVO.getData();

        String uid = (String) map.get("uid");
        Integer rid = (Integer) map.get("rid");
        if (StringUtils.isNotBlank(uid) && rid != null) {
            // 将用户id放入request中
            request.setAttribute("uid", uid);

            // 保存最后登录时间和IP
            try {
                String ip = HttpClientUtils.getRemoteAddr(request);
                String timestamp = System.currentTimeMillis() + "";
                Map<String, String> data = new HashMap<>(16);
                data.put("ip", ip);
                data.put("timestamp", timestamp);
                jedisClient.hset("last_login", uid, JsonUtils.mapToJson(data));
            } catch (Exception e) {
                log.error("缓存存储异常，错误位置：{}", "JwtAuthenticationFilter.UsernamePasswordAuthenticationToken()");
                e.printStackTrace();
            }

            // 设置角色
            authorities.add(new SimpleGrantedAuthority(RoleEnum.getMessage(rid)));

            // 这里直接注入角色，因为JWT已经验证了用户合法性，所以principal和credentials直接为null即可
            return new UsernamePasswordAuthenticationToken(null, null, authorities);
        }

        return null;
    }
    /**
     *  忽略目录
     * @param
     * @since 2022/9/8
     * @return
     */
    private boolean disProtectedUrl(HttpServletRequest request) {
        if (pathMatcher.match("/doc.html", request.getServletPath())) {
            return true;
        }
        if (pathMatcher.match("/auth/**", request.getServletPath())) {
            return true;
        }
        if (pathMatcher.match("/ws/**", request.getServletPath())) {
            return true;
        }
        if (pathMatcher.match("/swagger-resources/**", request.getServletPath())) {
            return true;
        }
        if (pathMatcher.match("/swagger-resources/**", request.getServletPath())) {
            return true;
        }
        if (pathMatcher.match("/v2/api-docs/**", request.getServletPath())) {
            return true;
        }


        return false;
    }
}
