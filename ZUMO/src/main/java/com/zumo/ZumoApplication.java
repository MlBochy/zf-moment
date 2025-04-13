package com.zumo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zumo.mapper")
public class ZumoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZumoApplication.class, args);
    }

}
