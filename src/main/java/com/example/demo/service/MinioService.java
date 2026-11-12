package com.example.demo.service;

import cn.hutool.crypto.digest.DigestUtil;
import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class MinioService {

    @Resource
    private MinioClient minioClient;

    public void putObject(String bucket, byte[] content, String objectName, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(content), content.length, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    public byte[] getObject(String bucket, String objectName) throws Exception {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build()
        )) {
            return StreamUtils.copyToByteArray(stream);
        }
    }

    /*
    * 判定文件是否存在
    * */

    public boolean objectExists(String bucket, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectName).build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /*
     * 保存谱图文件
     * */

    public String saveWithDedup(String bucket, byte[] content, String extension, String typePrefix) throws Exception {
        String sha256Hex = DigestUtil.sha256Hex(content);
        String subDir = sha256Hex.substring(0, Math.min(2, sha256Hex.length()));
        String objectName = typePrefix + subDir + "/" + sha256Hex + extension;

        if (objectExists(bucket, objectName)) {
            return objectName;
        }
        putObject(bucket, content, objectName, "application/octet-stream");
        return objectName;
    }

    /**
     * 设置桶策略
     * @param builder 策略json
     * @param bucketName 桶名称
     */
    @SneakyThrows(Exception.class)
    public void createBucketPolicy(StringBuilder builder, String bucketName) {
        if(builder.length()==0) builder=defaultBucketPolicy(bucketName);
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(builder.toString()).build());
    }

    /**
     * 获取默认桶策略
     * @param bucketName
     * @return
     */
    private StringBuilder defaultBucketPolicy(String bucketName){
        StringBuilder builder=new StringBuilder();
        builder.append("{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Action\": [\n" +
                "                \"s3:ListAllMyBuckets\",\n" +
                "                \"s3:ListBucket\",\n" +
                "                \"s3:GetBucketLocation\",\n" +
                "                \"s3:GetObject\",\n" +
                "                \"s3:PutObject\",\n" +
                "                \"s3:DeleteObject\"\n" +
                "      ],\n" +
                "      \"Principal\":\"*\",\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::"+bucketName+"/*\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        return builder;
    }

    public static String typePrefix(Integer type) {
        switch (type == null ? -1 : type) {
            case 0:  return "oil/";
            case 1:  return "detect/";
            default: return "other/";
        }
    }
}
