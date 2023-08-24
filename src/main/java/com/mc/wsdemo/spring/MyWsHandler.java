package com.mc.wsdemo.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * web socket 处理程序
 */
@Component
@Slf4j
public class MyWsHandler extends AbstractWebSocketHandler {
    private static Map<String, SessionBean> sessionBeanMap;
    private static AtomicInteger clientIdMaker;
    private static StringBuffer stringBuffer;

    static {
        sessionBeanMap = new ConcurrentHashMap<>(16);
        clientIdMaker = new AtomicInteger(0);
        stringBuffer = new StringBuffer();
    }

    /**
     * 连接建立
     *
     * @param session session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        SessionBean sessionBean = new SessionBean(session, clientIdMaker.getAndIncrement());
        sessionBeanMap.put(session.getId(), sessionBean);
        log.info(sessionBeanMap.get(session.getId()).getClientId() + " 创建连接.");
        stringBuffer.append(sessionBeanMap.get(session.getId()).getClientId() + " 进入了群聊.<br/>");
        sendMsg(sessionBeanMap);
    }

    private void sendMsg(Map<String, SessionBean> sessionBeanMap) {
        for (String key : sessionBeanMap.keySet()) {
            try {
                sessionBeanMap.get(key).getWebSocketSession().sendMessage(new TextMessage(stringBuffer.toString()));
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        log.info(sessionBeanMap.get(session.getId()).getClientId() + ":" + message.getPayload());
        stringBuffer.append(sessionBeanMap.get(session.getId()).getClientId() + ":" + message.getPayload() + ".<br/>");
        sendMsg(sessionBeanMap);

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        if (session.isOpen()) {
            session.close();
        }
        sessionBeanMap.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Integer clientId = sessionBeanMap.get(session.getId()).getClientId();
        sessionBeanMap.remove(session.getId());
        log.info(clientId + " 关闭连接.");
        stringBuffer.append(clientId + " 退出了群聊. <br/>");
        sendMsg(sessionBeanMap);
    }


//    @Scheduled(fixedRate = 2000)
//    public void sendMsg() throws IOException {
//        for (String key : sessionBeanMap.keySet()) {
//            sessionBeanMap.get(key).getWebSocketSession().sendMessage(new TextMessage("心跳"));
//        }
//    }

}
