package com.example.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.demo.domain.CmstSolublegasinoil;
import com.example.demo.domain.SolubleGasRecordMap;
import com.example.demo.mapper.CmstSolublegasinoilMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OilServiceImpl {


    @Autowired
    private CmstSolublegasinoilMapper cmstSolublegasinoilMapper;
    private static final Map<Integer, Integer> VOLTAGE_LEVEL_MAP = new HashMap<>();

    static {
        VOLTAGE_LEVEL_MAP.put(32, 110);
        VOLTAGE_LEVEL_MAP.put(33, 220);
        VOLTAGE_LEVEL_MAP.put(34, 330);
        VOLTAGE_LEVEL_MAP.put(36, 750);
        VOLTAGE_LEVEL_MAP.put(82, 800);
        VOLTAGE_LEVEL_MAP.put(85, 800);
        VOLTAGE_LEVEL_MAP.put(87, 800);
    }
    @Scheduled(initialDelay = 0, fixedRate = 400000 * 1000)
    public void getData(){
        List<CmstSolublegasinoil> list = cmstSolublegasinoilMapper.getCmstSolublegasinoilList();
        Map<String, List<CmstSolublegasinoil>> groupedMap = list.stream()
                .collect(Collectors.groupingBy(CmstSolublegasinoil::getDevicecode));
        groupedMap.entrySet().removeIf(entry -> {
            List<CmstSolublegasinoil> solublegasinoilList = entry.getValue();
            // 剔除不符合条件的数据
            boolean isValid = solublegasinoilList.size() >= 64 &&
                    VOLTAGE_LEVEL_MAP.get(solublegasinoilList.get(0).getVoltageGrade()) != null;
            return !isValid;
        });
        groupedMap.forEach((deviceCode, deviceList) -> {//升序
            deviceList.sort(Comparator.comparing(CmstSolublegasinoil::getAcquisitiontime));
        });
        for (Map.Entry<String, List<CmstSolublegasinoil>> entry : groupedMap.entrySet()) {
            String deviceKey = entry.getKey();
            List<CmstSolublegasinoil> dataList = entry.getValue();
            for (int i = 0; i <= dataList.size() - 64; i++) {
                List<CmstSolublegasinoil> values = dataList.subList(i, 64 + i);
//                if (values.get(values.size() - 1).getIsDetect()) {
//                    continue;
//                }
                if(values.get(values.size()-1).getObjId().equals("BAEB8D42-1C96-4A4E-B5CF-1AD297CF8A4C-56680")){
                    List<SolubleGasRecordMap> solubleGasRecordMaps = values.stream().map(oil -> {
                        SolubleGasRecordMap solubleGasRecordMap = BeanUtil.toBean(oil, SolubleGasRecordMap.class);
                        if (oil.getAcquisitiontime() != null) {
                            String time = DateUtil.format(oil.getAcquisitiontime(), "yyyy-MM-dd HH:mm:ss");
                            solubleGasRecordMap.setTime(time);
                        }
                        return solubleGasRecordMap;
                    }).collect(Collectors.toList());
                    List<Map<String, Object>> processedRecords = solubleGasRecordMaps.stream()
                            .map(record -> {
                                Map<String, Object> recordMap = BeanUtil.beanToMap(record);
                                recordMap.replaceAll((k, v) -> v == null ? "null" : v);
                                return recordMap;
                            })
                            .collect(Collectors.toList());
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("records", processedRecords);
                    dataMap.put("voltageLevel", "330");
                    String json = JSONUtil.toJsonStr(dataMap);

                    System.out.println(111);
                }
            }
        }
    }

}
