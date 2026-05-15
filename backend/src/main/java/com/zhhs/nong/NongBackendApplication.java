package com.zhhs.nong;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhhs.nong.mapper")
public class NongBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NongBackendApplication.class, args);
    }
}

