package com.gbq.docker.uiproject.commons.websocket;


import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.InputStream;

/**
 * @author 郭本琪
 * @description 输出线程，流使用线程输出，
 * @date 2022/9/11 17:25
 * @Copyright 总有一天，会见到成功
 */

public class OutPutThread extends Thread{
    private InputStream inputStream;
    private WebSocketSession session;

    public OutPutThread(InputStream inputStream, WebSocketSession session){
        super("OutPut"+ System.currentTimeMillis());
        this.session=session;
        this.inputStream=inputStream;
    }

    @Override
    public void run() {
        try{
            byte[] bytes=new byte[1024];
            while(!this.isInterrupted()){
                int n=inputStream.read(bytes);

                // 当n=-1时，代表连接关闭，例如用户输入exit
                if( n == -1) {
                    session.close();
                    return;
                }

                String msg=new String(bytes,0,n);
                session.sendMessage(new TextMessage(msg));
                bytes=new byte[1024];
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
