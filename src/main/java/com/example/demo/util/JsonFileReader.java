package com.example.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonFileReader {

    /**
     * 读取 resource 目录下的 JSON 文件为 JSONObject
     *
     * @param fileName JSON 文件名（不带路径，如 "config.json"）
     * @return 解析后的 JSONObject
     * @throws IOException 文件读取异常
     */
    public JSONObject readJsonFileAsObject(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        String jsonContent = readInputStreamToString(resource.getInputStream());
        return JSON.parseObject(jsonContent);
    }

    /**
     * 读取 resource 目录下的 JSON 文件为 JSONArray
     *
     * @param fileName JSON 文件名
     * @return 解析后的 JSONArray
     * @throws IOException 文件读取异常
     */
    public JSONArray readJsonFileAsArray(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        String jsonContent = readInputStreamToString(resource.getInputStream());
        return JSON.parseArray(jsonContent);
    }

    /**
     * 读取 resource 目录下的 JSON 文件为 JSON 字符串
     *
     * @param fileName JSON 文件名
     * @return JSON 字符串
     * @throws IOException 文件读取异常
     */
    public String readJsonFileAsString(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        return readInputStreamToString(resource.getInputStream());
    }

    /**
     * 读取 JSON 文件并转换为指定 Java 对象
     *
     * @param fileName JSON 文件名
     * @param clazz 目标 Java 类
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws IOException
     */
    public <T> T readJsonFileAsObject(String fileName, Class<T> clazz) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        String jsonContent = readInputStreamToString(resource.getInputStream());
        return JSON.parseObject(jsonContent, clazz);
    }

    /**
     * 读取 JSON 文件并转换为指定类型的 Java 列表
     *
     * @param fileName JSON 文件名
     * @param clazz 列表中元素的类型
     * @param <T> 泛型类型
     * @return 转换后的列表
     * @throws IOException
     */
    public <T> List<T> readJsonFileAsList(String fileName, Class<T> clazz) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        String jsonContent = readInputStreamToString(resource.getInputStream());
        return JSON.parseArray(jsonContent, clazz);
    }

    /**
     * Java 8 兼容的 InputStream 读取方法
     *
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException
     */
    private String readInputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8.name());
    }

    // 示例使用
    public static void main(String[] args) {
        JsonFileReader reader = new JsonFileReader();

        try {
            // 读取为 JSONArray
            JSONArray jsonArray = reader.readJsonFileAsArray("users.json");
            System.out.println("读取的 JSON 数组: " + jsonArray);

            // 读取为 Java 列表 (假设 User 类已定义)
            List<User> userList = reader.readJsonFileAsList("users.json", User.class);
            System.out.println("转换后的 User 列表: " + userList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 示例 User 类 (用于演示)
    public static class User {
        private String name;
        private int age;
        private String email;

        // 无参构造
        public User() {}

        // 有参构造
        public User(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}