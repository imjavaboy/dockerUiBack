package com.gbq.docker.uiproject.commons.websocket;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Socket;

/**
 * @author 郭本琪
 * @description WebSocket Session实体
 * @date 2022/9/11 17:24
 * @Copyright 总有一天，会见到成功
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecSession {
    private String ip;
    private String containerId;
    private Socket socket;
    private OutPutThread outPutThread;
}
