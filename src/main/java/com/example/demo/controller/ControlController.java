package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/control")
public class ControlController {

    /** 请求头里期望的 AppKey / AppSecret（和文档示例保持一致） */
    private static final String EXPECTED_APP_KEY = "AK_A8E73514A4504D";
    private static final String EXPECTED_APP_SECRET = "AS_E87D4711E8464B43";

    /**
     * 保存"遥调调用"返回的 requestId，供"遥调结果查询"使用
     * key = requestId, value = 标记（PENDING / SUCCESS）
     */
    private final Map<String, String> requestIdStore = new ConcurrentHashMap<>();

    /**
     * 统一入口，按 apiCode 分发
     */
    @PostMapping("/invoke")
    public JSONObject invoke(@RequestHeader Map<String, String> headers,
                             @RequestBody JSONObject body) {
        log.info("========== [模拟三区平台] 收到请求 ==========");
        log.info("Headers: {}", headers);
        log.info("Body   : {}", body.toJSONString());

        // 1. 鉴权（测试接口，校验从简）
        if (!validateAuth(headers)) {
            return buildError("鉴权失败：AppKey / AppSecret / yzAuthen 不匹配");
        }

        // 2. 取 apiCode
        String apiCode = body.getString("apiCode");
        String params = body.getString("params");
        if (apiCode == null || apiCode.isEmpty()) {
            return buildError("apiCode 不能为空");
        }

        // 3. 分发
        switch (apiCode) {
            case "executeMethod":
                return handleExecuteMethod(params);
            case "getMethodResult":
                return handleGetMethodResult(params);
            default:
                return buildError("不支持的 apiCode: " + apiCode);
        }
    }

    // ==================================================================
    // 接口一：遥调调用
    // ==================================================================
    /**
     * 对应文档：
     *   入参：apiCode=executeMethod, params=遥调参数
     *   出参：{ success: true, code: 200, msg: null, data: "<requestId>" }
     */
    private JSONObject handleExecuteMethod(String params) {
        log.info("---------- [接口一] 遥调调用 ----------");

        // 解析遥调参数（业务上不强制，仅打印方便调试）
        try {
            if (params != null && !params.isEmpty()) {
                JSONObject p = JSON.parseObject(params);
                String deviceIdentifier  = p.getString("deviceIdentifier");
                String methodIdentifier  = p.getString("methodIdentifier");
                String innerParams       = p.getString("params");

                log.info("  deviceIdentifier : {}", deviceIdentifier);
                log.info("  methodIdentifier : {}", methodIdentifier);
                log.info("  inner params     : {}", innerParams);

                // 内层再解析一次：methodId / params
                if (innerParams != null && !innerParams.isEmpty()) {
                    JSONObject inner = JSON.parseObject(innerParams);
                    String methodId = inner.getString("methodId");
                    log.info("  methodId         : {}", methodId);

                    if ("write".equals(methodId)) {
                        JSONObject writeParams = inner.getJSONObject("params");
                        if (writeParams != null) {
                            log.info("  【定值修改】dataReference={}, value={}",
                                    writeParams.getString("dataReference"),
                                    writeParams.getString("value"));
                        }
                    } else if ("startWork".equals(methodId)) {
                        log.info("  【复测】参数为空");
                    } else if ("triggerGI".equals(methodId)) {
                        log.info("  【总召】参数为空");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("params 解析失败，按原样透传", e);
        }

        // 生成一个 requestId（模拟第三方返回的"响应ID"）
        String requestId = UUID.randomUUID().toString();
        requestIdStore.put(requestId, "PENDING");
        log.info("  → 生成 requestId = {}", requestId);

        // 按文档格式返回
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("code", 200);
        result.put("msg", null);
        result.put("data", requestId);
        return result;
    }

    // ==================================================================
    // 接口二：遥调结果查询
    // ==================================================================
    /**
     * 对应文档：
     *   入参：apiCode=getMethodResult, params={"requestId":"xxx"}
     *   出参：{ success: true, code: 200, msg: null, data: {result, requestId, success} }
     */
    private JSONObject handleGetMethodResult(String params) {
        log.info("---------- [接口二] 遥调结果查询 ----------");

        String requestId = null;
        try {
            if (params != null && !params.isEmpty()) {
                JSONObject p = JSON.parseObject(params);
                requestId = p.getString("requestId");
            }
        } catch (Exception e) {
            log.warn("params 解析失败", e);
        }
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        log.info("  查询 requestId = {}", requestId);

        // 标记为已完成
        requestIdStore.put(requestId, "SUCCESS");

        // ============ 构造最内层：站端平台与装置的通讯结果 ============
        // 对应文档：1.1.1.1 result
        JSONObject innerResult = new JSONObject();
        innerResult.put("requestId", UUID.randomUUID().toString());
        innerResult.put("methodId", "triggerGI");
        innerResult.put("topic", "iotcdc/sys/thing/product-ssysp-gateway/YZBYHJCBDZYSP0501/method/subscribe");
        innerResult.put("result", "2000");                    // 2000 = 成功
        innerResult.put("replyMessage", "GI triggered successfully");
        innerResult.put("expireTime", "10000");

        // 对应文档：1.1.1 data
        JSONObject stationData = new JSONObject();
        stationData.put("result", innerResult.toJSONString());
        stationData.put("requestId", innerResult.getString("requestId"));
        stationData.put("success", "1");

        // 对应文档：1.1 replyMessage
        JSONObject stationReply = new JSONObject();
        stationReply.put("code", 200);
        stationReply.put("data", stationData);
        stationReply.put("msg", null);
        stationReply.put("success", true);

        // ============ 构造外层 result ============
        JSONObject outerResult = new JSONObject();
        outerResult.put("requestId", requestId);
        outerResult.put("methodId", "executeMethod");
        outerResult.put("topic", "iotcdc/sys/thing/product-ssysp-gateway/SSYZBYHJCBDZYSP0501/method/subscribe");
        outerResult.put("expireTime", "10000");
        outerResult.put("result", 2000);                       // 2000 = 成功
        outerResult.put("replyMessage", stationReply.toJSONString());

        // ============ 构造外层 data ============
        JSONObject data = new JSONObject();
        data.put("result", outerResult.toJSONString());
        data.put("requestId", requestId);
        data.put("success", "1");

        // ============ 最终响应 ============
        JSONObject response = new JSONObject();
        response.put("success", true);
        response.put("code", 200);
        response.put("msg", null);
        response.put("data", data);

        log.info("  → 返回结果: {}", data.toJSONString());
        return response;
    }

    // ==================================================================
    // 工具方法
    // ==================================================================

    /**
     * 简单校验请求头（header 名不区分大小写）
     */
    private boolean validateAuth(Map<String, String> headers) {
        Map<String, String> lower = new HashMap<>();
        headers.forEach((k, v) -> lower.put(k.toLowerCase(), v));

        String appKey    = lower.get("appkey");
        String appSecret = lower.get("appsecret");
        String yzAuthen  = lower.get("yzauthen");
        String source    = lower.get("yzrequestsource");

        if (appKey == null || appSecret == null || yzAuthen == null) {
            log.warn("缺少必需的请求头：appKey={}, appSecret={}, yzAuthen={}",
                    appKey, appSecret, yzAuthen);
            return false;
        }

        // 测试环境：只要 AppKey 和 AppSecret 匹配就放行
        // 生产环境应该按照文档校验 yzAuthen 的 sha256
        boolean ok = EXPECTED_APP_KEY.equals(appKey)
                && EXPECTED_APP_SECRET.equals(appSecret);
        if (!ok) {
            log.warn("AppKey / AppSecret 不匹配: appKey={}, appSecret={}", appKey, appSecret);
        }
        return ok;
    }

    /**
     * 统一错误响应
     */
    private JSONObject buildError(String msg) {
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("code", 500);
        result.put("msg", msg);
        result.put("data", null);
        return result;
    }
}
