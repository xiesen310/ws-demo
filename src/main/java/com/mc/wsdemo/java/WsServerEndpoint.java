package com.mc.wsdemo.java;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/myWs")
@Component
@Slf4j
public class WsServerEndpoint {

    static Map<String, Session> sessionMap = new ConcurrentHashMap<>(16);

    @OnOpen
    public void onOpen(Session session) {
        sessionMap.put(session.getId(), session);
        log.info("websocket is open.");
    }

    @OnMessage
    public String onMessage(String text) {
        log.info("收到了一条消息: " + text);
        return "已经收到你的消息.";
    }

    @OnClose
    public void onClose(Session session) {
        sessionMap.remove(session.getId());
        log.info("websocket is close.");
    }


    @Scheduled(fixedRate = 2000)
    public void sendMsg() throws IOException {
        for (String key : sessionMap.keySet()) {
            sessionMap.get(key).getBasicRemote().sendText("心跳");
        }
    }
}
