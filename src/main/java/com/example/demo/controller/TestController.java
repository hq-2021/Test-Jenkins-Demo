package com.example.demo.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.util.JsonFileReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@RestController
@RequestMapping("/test")
public class TestController extends Thread{

    private final JsonFileReader reader = new JsonFileReader();

    // ========== 公共辅助方法 ==========

    /**
     * 构建统一的基础响应体
     */
    private JSONObject buildBaseResponse() {
        JSONObject response = new JSONObject();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "success");
        return response;
    }

    /**
     * 从指定文件读取 JSON 数组
     */
    private JSONArray readJsonArrayFromFile(String filePath) throws IOException {
        return reader.readJsonFileAsArray(filePath);
    }

    /**
     * 通用处理方法：读取文件 -> 过滤 -> 封装分页 -> 返回完整响应
     *
     * @param filePath   JSON 文件路径
     * @param filter     过滤条件（可为空，表示不过滤）
     * @param pageSize   每页大小（固定为 1000，可根据需要调整）
     * @return           封装后的响应对象
     */
    private JSONObject processData(String filePath, Predicate<JSONObject> filter, int pageSize) throws IOException {
        JSONArray jsonArray = readJsonArrayFromFile(filePath);
        List<JSONObject> itemList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            if (filter == null || filter.test(item)) {
                itemList.add(item);
            }
        }

        // 构造分页信息
        JSONObject pageInfo = new JSONObject();
        pageInfo.put("total", itemList.size());
        pageInfo.put("pageIndex", 1);
        pageInfo.put("pageSize", pageSize);

        JSONObject resultObject = new JSONObject();
        resultObject.put("errorCode", null);
        resultObject.put("pageInfo", pageInfo);
        resultObject.put("list", itemList);

        JSONObject response = buildBaseResponse();
        response.put("resultObject", resultObject);
        return response;
    }

    // 重载：默认 pageSize = 1000
    private JSONObject processData(String filePath, Predicate<JSONObject> filter) throws IOException {
        return processData(filePath, filter, 1000);
    }

    // ========== 业务接口 ==========


    @PostMapping("wlw_oil_chromato_monitor_data_wide_all")
    public JSONObject data5(@RequestBody JSONObject requestData) throws IOException, InterruptedException {
        String deviceId = requestData.getString("device_id");

        String testTimeStr  = requestData.getString("testTime");
        String timestampStr = requestData.getString("timestamp");

        boolean hasTestTime  = StrUtil.isNotBlank(testTimeStr);
        boolean hasTimestamp = StrUtil.isNotBlank(timestampStr);

        // 两个都没传：只按 device_id 过滤
        if (!hasTestTime && !hasTimestamp) {
            return processData("file/offline.json", item -> false);
        }

        // 哪个非空用哪个作为筛选字段（两个都传时 testTime 优先，见下方说明）
        String timeField = hasTestTime ? "testTime" : "timestamp";
        String paramStr  = hasTestTime ? testTimeStr : timestampStr;

        JSONObject rangeObj = JSON.parseObject(paramStr);
        if (rangeObj == null) {
            Predicate<JSONObject> filter = item ->
                    deviceId == null || deviceId.equals(item.getString("device_id"));
            return processData("file/offline.json", filter);
        }

        String columnLeft  = rangeObj.getString("columnLeft");
        String columnRight = rangeObj.getString("columnRight");

        Date left  = parseItemTime(columnLeft);
        Date right = parseItemTime(columnRight);

        // 范围解析失败：只按 device_id 过滤（也可按业务改成返回空结果）
        if (left == null || right == null) {
            Predicate<JSONObject> filter = item ->
                    deviceId == null || deviceId.equals(item.getString("device_id"));
            return processData("file/offline.json", filter);
        }

        final String field = timeField;
        Predicate<JSONObject> filter = item -> {
            String timeStr = item.getString(field);
            if (StrUtil.isBlank(timeStr)) {
                return false;
            }
            Date itemTime = parseItemTime(timeStr);
            if (itemTime == null) {
                return false;
            }

            boolean timeMatch   = itemTime.after(left) && itemTime.before(right);
            boolean deviceMatch = (deviceId == null) || deviceId.equals(item.getString("device_id"));
            return timeMatch && deviceMatch;
        };

        return processData("file/offline.json", filter);
    }

    private Date parseItemTime(String str) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        str = str.trim();
        try {
            if (str.matches("\\d{14}")) {
                return DateUtil.parse(str, "yyyyMMddHHmmss");
            }
            return DateUtil.parseDateTime(str);
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("wlw_oil_chromato_monitor_data_wide_all_zt")
    public JSONObject data89(@RequestBody JSONObject requestData) throws IOException, InterruptedException {
        String deviceId = requestData.getString("device_id");

        String testTimeStr  = requestData.getString("testTime");
        String timestampStr = requestData.getString("timestamp");

        boolean hasTestTime  = StrUtil.isNotBlank(testTimeStr);
        boolean hasTimestamp = StrUtil.isNotBlank(timestampStr);

        // 两个都没传：只按 device_id 过滤
        if (!hasTestTime && !hasTimestamp) {
            Predicate<JSONObject> filter = item ->
                    deviceId == null || deviceId.equals(item.getString("device_id"));
            return processData("file/offlineZt.json", item -> false);
        }

        // 哪个非空用哪个作为筛选字段（两个都传时 testTime 优先，见下方说明）
        String timeField = hasTestTime ? "testTime" : "timestamp";
        String paramStr  = hasTestTime ? testTimeStr : timestampStr;

        JSONObject rangeObj = JSON.parseObject(paramStr);
        if (rangeObj == null) {
            Predicate<JSONObject> filter = item ->
                    deviceId == null || deviceId.equals(item.getString("device_id"));
            return processData("file/offlineZt.json", filter);
        }

        String columnLeft  = rangeObj.getString("columnLeft");
        String columnRight = rangeObj.getString("columnRight");

        Date left  = parseItemTime(columnLeft);
        Date right = parseItemTime(columnRight);

        // 范围解析失败：只按 device_id 过滤（也可按业务改成返回空结果）
        if (left == null || right == null) {
            Predicate<JSONObject> filter = item ->
                    deviceId == null || deviceId.equals(item.getString("device_id"));
            return processData("file/offlineZt.json", filter);
        }

        final String field = timeField;
        Predicate<JSONObject> filter = item -> {
            String timeStr = item.getString(field);
            if (StrUtil.isBlank(timeStr)) {
                return false;
            }
            Date itemTime = parseItemTime(timeStr);
            if (itemTime == null) {
                return false;
            }

            boolean timeMatch   = itemTime.after(left) && itemTime.before(right);
            boolean deviceMatch = (deviceId == null) || deviceId.equals(item.getString("device_id"));
            return timeMatch && deviceMatch;
        };

        return processData("file/offlineZt.json", filter);
    }

    @PostMapping("wlw_oil_chromato_stdanded_data_wide_all")
    public JSONObject data511(@RequestBody JSONObject requestData) throws IOException, InterruptedException {
        String deviceId = requestData.getString("device_id");

        String testTimeStr  = requestData.getString("analysisTime");
        String timestampStr = requestData.getString("timestamp");

        boolean hasTestTime  = StrUtil.isNotBlank(testTimeStr);
        boolean hasTimestamp = StrUtil.isNotBlank(timestampStr);

        // 两个都没传：只按 device_id 过滤
        if (!hasTestTime && !hasTimestamp) {
            return processData("file/sample.json", item -> false);
        }

        // 哪个非空用哪个作为筛选字段（两个都传时 testTime 优先，见下方说明）
        String timeField = hasTestTime ? "analysisTime" : "timestamp";
        String paramStr  = hasTestTime ? testTimeStr : timestampStr;

        JSONObject rangeObj = JSON.parseObject(paramStr);
        if (rangeObj == null) {
            return processData("file/sample.json", item -> false);
        }

        String columnLeft  = rangeObj.getString("columnLeft");
        String columnRight = rangeObj.getString("columnRight");

        Date left  = parseItemTime(columnLeft);
        Date right = parseItemTime(columnRight);

        // 范围解析失败：只按 device_id 过滤（也可按业务改成返回空结果）
        if (left == null || right == null) {
            Predicate<JSONObject> filter = item ->
                    deviceId == null || deviceId.equals(item.getString("device_id"));
            return processData("file/sample.json", filter);
        }

        final String field = timeField;
        Predicate<JSONObject> filter = item -> {
            String timeStr = item.getString(field);
            if (StrUtil.isBlank(timeStr)) {
                return false;
            }
            Date itemTime = parseItemTime(timeStr);
            if (itemTime == null) {
                return false;
            }

            boolean timeMatch   = itemTime.after(left) && itemTime.before(right);
            boolean deviceMatch = (deviceId == null) || deviceId.equals(item.getString("device_id"));
            return timeMatch && deviceMatch;
        };

        return processData("file/sample.json", filter);
    }

    @PostMapping("wlw_oil_chromato_stdanded_data_wide_all_zt")
    public JSONObject data51(@RequestBody JSONObject requestData) throws IOException, InterruptedException {
        String deviceId = requestData.getString("device_id");

        String testTimeStr  = requestData.getString("analysisTime");
        String timestampStr = requestData.getString("timestamp");

        boolean hasTestTime  = StrUtil.isNotBlank(testTimeStr);
        boolean hasTimestamp = StrUtil.isNotBlank(timestampStr);

        // 两个都没传：只按 device_id 过滤
        if (!hasTestTime && !hasTimestamp) {
            return processData("file/sampleZt.json", item -> false);
        }

        // 哪个非空用哪个作为筛选字段（两个都传时 testTime 优先，见下方说明）
        String timeField = hasTestTime ? "analysisTime" : "timestamp";
        String paramStr  = hasTestTime ? testTimeStr : timestampStr;

        JSONObject rangeObj = JSON.parseObject(paramStr);
        if (rangeObj == null) {
            return processData("file/sampleZt.json", item -> false);
        }

        String columnLeft  = rangeObj.getString("columnLeft");
        String columnRight = rangeObj.getString("columnRight");

        Date left  = parseItemTime(columnLeft);
        Date right = parseItemTime(columnRight);

        // 范围解析失败：只按 device_id 过滤（也可按业务改成返回空结果）
        if (left == null || right == null) {
            Predicate<JSONObject> filter = item ->
                    deviceId == null || deviceId.equals(item.getString("device_id"));
            return processData("file/sampleZt.json", filter);
        }

        final String field = timeField;
        Predicate<JSONObject> filter = item -> {
            String timeStr = item.getString(field);
            if (StrUtil.isBlank(timeStr)) {
                return false;
            }
            Date itemTime = parseItemTime(timeStr);
            if (itemTime == null) {
                return false;
            }

            boolean timeMatch   = itemTime.after(left) && itemTime.before(right);
            boolean deviceMatch = (deviceId == null) || deviceId.equals(item.getString("device_id"));
            return timeMatch && deviceMatch;
        };

        return processData("file/sampleZt.json", filter);
    }

    @PostMapping("wlw_oil_chromato_stdanded_cogroup_data_wide_all")
    public JSONObject data7(@RequestBody JSONObject requestData) throws IOException {
        String deviceId = requestData.getString("device_id");
        String batchId = requestData.getString("batch_id");
        Predicate<JSONObject> filter = item -> deviceId != null && deviceId.equals(item.getString("device_id"))
                && batchId != null && batchId.equals(item.getString("batch_id"));
        return processData("file/sampleDetail.json", filter);
    }

    @PostMapping("wlw_oil_chromato_stdanded_cogroup_data_wide_all_zt")
    public JSONObject data71(@RequestBody JSONObject requestData) throws IOException {
        String deviceId = requestData.getString("device_id");
        String batchId = requestData.getString("batch_id");
        Predicate<JSONObject> filter = item -> deviceId != null && deviceId.equals(item.getString("device_id"))
                && batchId != null && batchId.equals(item.getString("batch_id"));
        return processData("file/sampleDetailZt.json", filter);
    }

    @PostMapping("common_iot_gc_arc")
    public JSONObject data8(@RequestBody JSONObject requestData) throws IOException {
        // 无过滤，返回全部
        return processData("file/device.json", null);
    }

    @PostMapping("common_wide_archives")
    public JSONObject data9(@RequestBody JSONObject requestData) throws IOException {
        String astId = requestData.getString("ast_id");
        Predicate<JSONObject> filter = item -> astId != null && astId.equals(item.getString("ast_id"));
        return processData("file/tran.json", filter);
    }

    @PostMapping("station")
    public JSONObject data10(@RequestBody JSONObject requestData) throws IOException {
        String astId = requestData.getString("ast_id");
        Predicate<JSONObject> filter = item -> astId != null && astId.equals(item.getString("ast_id"));
        return processData("file/station.json", filter);
    }

    @PostMapping("work")
    public JSONObject data11(@RequestBody JSONObject requestData) throws IOException {
        String deviceId = requestData.getString("device_id");
        Predicate<JSONObject> filter = item -> deviceId != null && deviceId.equals(item.getString("device_id"));
        return processData("file/work.json", filter, 1);
    }

    @PostMapping("wlw_inst_workingcondition_device_status")
    public JSONObject data12(@RequestBody JSONObject requestData) throws IOException {
        return processData("file/status.json", null);
    }

    @PostMapping("ads_oil_dga_collect_wide_day")
    public JSONObject data13(@RequestBody JSONObject requestData) throws IOException, InterruptedException {
        JSONObject analysisTime = JSON.parseObject(requestData.getString("data_time"));
        String columnLeft = analysisTime.getString("columnLeft");
        String columnRight = analysisTime.getString("columnRight");
        Thread.sleep(10000);
        Predicate<JSONObject> filter = item -> {
            return DateUtil.parseDateTime(item.getString("data_time"))
                    .after(DateUtil.parseDateTime(columnLeft))
                    && DateUtil.parseDateTime(item.getString("data_time"))
                    .before(DateUtil.parseDateTime(columnRight));
        };
        return processData("file/oil.json", filter);
    }

    @PostMapping("geo")
    public JSONObject data14(@RequestBody JSONObject requestData) throws IOException {
        return processData("file/geo.json", null);
    }

    @PostMapping("history")
    public JSONObject data15(@RequestBody JSONObject requestData) throws IOException {
        String deviceCode = requestData.getString("cmst_tran_devicecode");
        Predicate<JSONObject> filter = item -> deviceCode != null && deviceCode.equals(item.getString("cmst_tran_devicecode"));
        return processData("file/history.json", filter);
    }

    @PostMapping("ads_oil_dga_chromatogram_data")
    public JSONObject data16(@RequestBody JSONObject requestData) throws IOException {
        String deviceId = requestData.getString("deviceId");
        JSONObject analysisTime = JSON.parseObject(requestData.getString("datatime"));
        String columnLeft = analysisTime.getString("columnLeft");
        String columnRight = analysisTime.getString("columnRight");


        JSONArray jsonArray = readJsonArrayFromFile("file/pt.json");
        List<JSONObject> itemList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            String ts = item.getString("TIMESTAMP");
            if (ts == null) continue;
            if (!ts.equals(columnLeft)) continue;
            String did = item.getString("DEVICE_ID");
            if (deviceId != null && !deviceId.equals(did)) continue;
            itemList.add(item);
        }
        // 构造分页信息
        JSONObject pageInfo = new JSONObject();
        pageInfo.put("total", itemList.size());
        pageInfo.put("pageIndex", 1);
        pageInfo.put("pageSize", 100);

        JSONObject resultObject = new JSONObject();
        resultObject.put("errorCode", null);
        resultObject.put("pageInfo", pageInfo);
        resultObject.put("list", itemList);

        JSONObject response = buildBaseResponse();
        response.put("resultObject", resultObject);
        System.out.println(5*4 + 7*4 + 6);
        return response;
    }

    @PostMapping("new_wlw_oil_chromato_monitor_data_wide_2026")
    public JSONObject data17(@RequestBody JSONObject requestData) throws IOException {
        String astId = requestData.getString("batch_id");
        Predicate<JSONObject> filter = item -> astId != null && astId.equals(item.getString("batch_id"));
        return processData("file/wave.json", filter);
    }

}