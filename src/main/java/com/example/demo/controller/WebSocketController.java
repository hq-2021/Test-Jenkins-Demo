package com.example.demo.controller;// 3. 控制器（可选，用于HTTP接口）

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.demo.handler.MyWebSocketHandler;
import com.example.demo.util.JsonResult;
import com.example.demo.util.ResultUtils;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketController {
    
    @GetMapping("/onlineUsers")
    public JsonResult getOnlineUsers() {
        Set<String> users = MyWebSocketHandler.getOnlineUsers();
        return ResultUtils.ok(users);
    }
    
    @PostMapping("/broadcast")
    public JsonResult broadcast(@RequestBody BroadcastRequest request) {
        String message = JSONUtil.createObj()
            .put("type", "broadcast")
            .put("content", request.getContent())
            .put("time", DateUtil.now())
            .toString();
        
        MyWebSocketHandler.broadcastToAll(message);
        return ResultUtils.ok("广播发送成功");
    }
    
    // 请求对象
    @Data
    static class BroadcastRequest {
        private String content;
    }

}