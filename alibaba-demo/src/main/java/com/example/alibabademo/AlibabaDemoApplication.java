package com.example.alibabademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import javax.annotation.Resource;

@EnableFeignClients
@SpringBootApplication
public class AlibabaDemoApplication {

    @Resource
    private HelloService helloService;

    public static void main(String[] args) {
        SpringApplication.run(AlibabaDemoApplication.class, args);
    }

}
