package com.example.demo.handler;// 2. WebSocket处理器（使用Hutool工具）

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MyWebSocketHandler extends TextWebSocketHandler {
    
    // 存储所有连接的会话
    private static final Set<WebSocketSession> sessions = new ConcurrentHashSet<>();
    // 存储用户ID和会话的映射
    private static final ConcurrentHashMap<String, WebSocketSession> userSessions = 
        new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("新连接建立: " + session.getId());
        
        // 发送欢迎消息
        JSONObject welcome = JSONUtil.createObj()
            .put("type", "system")
            .put("message", "连接成功")
            .put("time", DateUtil.now());
        sendMessage(session, welcome.toString());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);
        
        // 使用Hutool解析JSON
        if (JSONUtil.isJson(payload)) {
            JSONObject json = JSONUtil.parseObj(payload);
            String type = json.getStr("type");
            String userId = json.getStr("userId");
            
            // 用户登录
            if ("login".equals(type)) {
                userSessions.put(userId, session);
                broadcastSystemMessage(userId + " 已上线");
            }
            // 发送消息
            else if ("message".equals(type)) {
                String content = json.getStr("content");
                String toUserId = json.getStr("toUserId");
                
                JSONObject msgJson = JSONUtil.createObj()
                    .put("type", "message")
                    .put("from", userId)
                    .put("content", content)
                    .put("time", DateUtil.now());
                
                // 私聊
                if (StrUtil.isNotBlank(toUserId)) {
                    sendToUser(toUserId, msgJson.toString());
                }
                // 群发
                else {
                    broadcastMessage(msgJson.toString(), session);
                }
            }
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // 从用户映射中移除
        userSessions.forEach((userId, userSession) -> {
            if (userSession == session) {
                userSessions.remove(userId);
                broadcastSystemMessage(userId + " 已离线");
            }
        });
        System.out.println("连接关闭: " + session.getId());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("传输错误: " + exception.getMessage());
    }
    
    // 发送消息给指定用户
    private void sendToUser(String userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }
    
    // 广播消息（排除发送者）
    private void broadcastMessage(String message, WebSocketSession excludeSession) {
        sessions.forEach(session -> {
            if (session.isOpen() && !session.equals(excludeSession)) {
                sendMessage(session, message);
            }
        });
    }
    
    // 广播系统消息
    private void broadcastSystemMessage(String content) {
        JSONObject systemMsg = JSONUtil.createObj()
            .put("type", "system")
            .put("content", content)
            .put("time", DateUtil.now());
        
        String message = systemMsg.toString();
        sessions.forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }
    
    // 发送消息
    private void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
        }
    }
    
    // 获取所有在线用户
    public static Set<String> getOnlineUsers() {
        return userSessions.keySet();
    }
    
    // 发送消息给所有用户
    public static void broadcastToAll(String message) {
        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}