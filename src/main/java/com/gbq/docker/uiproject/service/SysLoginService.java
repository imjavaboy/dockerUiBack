package com.gbq.docker.uiproject.service;


import com.baomidou.mybatisplus.service.IService;
import com.gbq.docker.uiproject.domain.entity.SysLogin;
import com.gbq.docker.uiproject.domain.vo.ResultVO;

/**
 * @author 郭本琪
 * @description 登录服务
 * @date 2022/9/7
 * @Copyright 总有一天，会见到成功
 */
public interface SysLoginService extends IService<SysLogin> {



    /**
     *  根据ID获取用户
     * @param id 用户id
     * @since 2022/9/7
     * @return
     */
    SysLogin getById(String id);

    /**
     *  根据用户名获取用户
     * @param
     * @since 2022/9/8
     * @return
     */
    SysLogin getByUserName(String username);

    /**
     *  验证密码
     * @param
     * @since 2022/9/8
     * @return
     */
    boolean checkPassword(String username, String password);

    /**
     *  判断用户是否被冻结
     * @param
     * @since 2022/9/8
     * @return
     */
    boolean hasFreeze(String username);

    /**
     *  注册校验
     * @param
     * @since 2022/9/8
     * @return
     */
    ResultVO registerCheck(String username, String email);

    /**
     *  根据邮件获取用户
     * @param
     * @since 2022/9/8
     * @return
     */
    SysLogin getByEmail(String email);

    /**
     *  保存用户信息到数据库
     * @param
     * @since 2022/9/8
     * @return
     */
    boolean save(SysLogin sysLogin);

    /**
     *  发送注册邮件
     * @param
     * @since 2022/9/8
     * @return
     */
    Boolean sendRegisterEmial(String email);

    /**
     *  验证注册邮件
     * @param
     * @since 2022/9/8
     * @return
     */
    Boolean verifyRegisterEmail(String token);

    /**
     *  清理用户缓存
     * @param
     * @since 2022/9/8
     * @return
     */
    void cleanLoginCache(SysLogin login);

    /**
     *  冻结用户
     * @param
     * @since 2022/9/9
     * @return
     */
    int freezeUser(String[] ids);


    /**
     *  更新用户信息
     * @param
     * @since 2022/9/9
     * @return
     */
    int update(SysLogin sysLogin);

    /**
     *  解冻用户
     * @param
     * @since 2022/9/9
     * @return
     */
    int cancelFreezeUser(String[] ids);
}
