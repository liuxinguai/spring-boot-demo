package com.example.alibabademo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("feign-hello")
public interface HelloService {

    @GetMapping("hello")
    public String hello(String message);
}
