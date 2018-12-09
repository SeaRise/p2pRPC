package com.rpc.example.server;

import com.rpc.common.RpcService;
import com.rpc.example.api.HelloService;

@RpcService(value = HelloService.class, version = "sample.hello2")
public class HelloServiceImpl2 implements HelloService {

    @Override
    public String hello(String name) {
        return "你好! " + name;
    }
}
