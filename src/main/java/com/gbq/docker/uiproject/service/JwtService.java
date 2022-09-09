package com.gbq.docker.uiproject.service;


import com.gbq.docker.uiproject.domain.vo.ResultVO;
import com.gbq.docker.uiproject.domain.vo.UserVO;

/**
 * @author 郭本琪
 * @description jwt
 * @date 2022/9/8 12:14
 * @Copyright 总有一天，会见到成功
 */
public interface JwtService {

    /**
     *  生成token
     * @param
     * @since 2022/9/8
     * @return
     */
    String genToken(String username);

    /**
     *  读取用户信息
     * @param
     * @since 2022/9/8
     * @return
     */
    UserVO getUserInfo(String token);

    /**
     *  校验token
     * @param
     * @since 2022/9/8
     * @return
     */
    ResultVO checkToken(String token);

    /**
     *  删除token
     * @param
     * @since 2022/9/9
     * @return
     */
    ResultVO deleteToken(String username);

    /**
     *  获取所有token
     * @param
     * @since 2022/9/9
     * @return
     */
    ResultVO listToken();
}
