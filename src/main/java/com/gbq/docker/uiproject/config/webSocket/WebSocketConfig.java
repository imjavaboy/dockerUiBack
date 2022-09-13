package com.gbq.docker.uiproject.config.webSocket;


import com.gbq.docker.uiproject.commons.websocket.ContainerExecHandshakeInterceptor;
import com.gbq.docker.uiproject.commons.websocket.ContainerExecWSHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author 郭本琪
 * @description websocketConfig 配置
 * @date 2022/9/11 15:58
 * @Copyright 总有一天，会见到成功
 */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ServerEndpointExporter serverEndpointExporter(ApplicationContext context) {
        return new ServerEndpointExporter();
    }

    @Bean
    public ContainerExecWSHandler containerExecWSHandler() {
        return new ContainerExecWSHandler();
    }

    /**
     *
     * 1.实现WebSocketConfigurer接口，重写registerWebSocketHandlers方法，这是一个核心实现方法，
     * 配置websocket入口，允许访问的域、注册Handler、SockJs支持和拦截器。
     * 2.registry.addHandler注册和路由的功能，当客户端发起websocket连接，把/path交给对应的handler
     * 处理，而不实现具体的业务逻辑，可以理解为收集和任务分发中心。
     * 3.setAllowedOrigins(String[] domains),允许指定的域名或IP(含端口号)建立长连接，如果只允许
     * 自家域名访问，这里轻松设置。如果不限时使用"*"号，如果指定了域名，则必须要以http或https开头。
     * 4.addInterceptors，顾名思义就是为handler添加拦截器，可以在调用handler前后加入我们自己的逻辑代码。
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(containerExecWSHandler(),"/ws/container/exec")
                .addInterceptors(new ContainerExecHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
