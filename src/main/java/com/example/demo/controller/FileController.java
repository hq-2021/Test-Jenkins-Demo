package com.example.demo.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/file")
public class FileController {

    @Value("${minio.bucketFile}")
    private String bucket;

    @Value("${minio.ptBucket}")
    private String ptBucket;

    @Resource
    private MinioService minioService;

    @PostMapping("upload")
    public JSONObject upload(@RequestParam("file") MultipartFile file) {
        JSONObject result = new JSONObject();
        try {
            String originalName = file.getOriginalFilename();
            String suffix = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";
            String objectName = UUID.randomUUID() + "-" + DateTime.now().toString("yyyyMMddHHmmss") + suffix;

            minioService.putObject(bucket, file.getBytes(), objectName, file.getContentType());

            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("fileName", objectName);
            result.put("originalName", originalName);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "上传失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("download/{objectName}")
    public ResponseEntity<byte[]> download(@PathVariable String objectName) {
        try {
            byte[] data = minioService.getObject(bucket, objectName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(objectName, "UTF-8"));

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("saveAsp")
    public String saveAsp(String hexData, Integer type) {
        try {
            if (StrUtil.isBlank(hexData) || "null".equals(hexData)) {
                return null;
            }
            if (hexData.length() % 2 != 0) {
                return null;
            }
            if (!hexData.matches("^[0-9a-fA-F]+$")) {
                return null;
            }
            byte[] rawBytes = HexUtil.decodeHex(hexData);
            if (rawBytes == null || rawBytes.length == 0) {
                return null;
            }
            return minioService.saveWithDedup(ptBucket, rawBytes, ".bin", MinioService.typePrefix(type));
        } catch (IllegalArgumentException e) {
            log.error("十六进制字符串格式错误", e);
        } catch (Exception e) {
            log.error("谱图文件写入失败", e);
        }
        return null;
    }

}
