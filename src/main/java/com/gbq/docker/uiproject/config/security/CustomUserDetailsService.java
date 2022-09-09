package com.gbq.docker.uiproject.config.security;


import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.enums.RoleEnum;
import com.gbq.docker.uiproject.service.SysLoginService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author 郭本琪
 * @description
 * @date 2022/9/8 8:26
 * @Copyright 总有一天，会见到成功
 */
@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {
    @Resource
    private SysLoginService loginService;


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        SysLogin loginUser = loginService.getByUserName(s);

        if (loginUser == null){
            throw  new UsernameNotFoundException("用户名不存在");
        }
        String roleName = RoleEnum.getMessage(loginUser.getRoleId());
        authorities.add(new SimpleGrantedAuthority(roleName));

        return new User(loginUser.getUsername(),loginUser.getPassword(),authorities);
    }
}
