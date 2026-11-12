package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {
//    @PostConstruct
//    void started() {
//        // 设置 JVM 默认时区
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//        System.out.println("时区已设置为: " + TimeZone.getDefault().getID());
//    }
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(DemoApplication.class, args);
    }

}
