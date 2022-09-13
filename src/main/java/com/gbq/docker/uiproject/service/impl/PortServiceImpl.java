package com.gbq.docker.uiproject.service.impl;


import com.gbq.docker.uiproject.commons.util.RandomUtils;
import com.gbq.docker.uiproject.service.PortService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author 郭本琪
 * @description 端口服务
 * @date 2022/9/12 10:08
 * @Copyright 总有一天，会见到成功
 */
@Service
@Slf4j
public class PortServiceImpl implements PortService {
    @Value("${docker.server.address}")
    private String host;

    @Override
    public boolean hasUse(Integer port) {
        boolean flag = true;
        try {
            flag = isPortUsing(host, port);
        } catch (Exception e) {
        }

        return flag;
    }

    private boolean isPortUsing(String host, Integer port) throws UnknownHostException {
        boolean flag = false;
        InetAddress theAddress = InetAddress.getByName(host);
        try {
            Socket socket = new Socket(theAddress,port);
            flag = true;
        } catch (IOException e) {
            // 异常说明被占用
        }
        return flag;
    }

    @Override
    public Integer randomPort() {
        int port;
        do {
            port = RandomUtils.integer(10000, 65535);
        }while (hasUse(port));
        return port;
    }
}
