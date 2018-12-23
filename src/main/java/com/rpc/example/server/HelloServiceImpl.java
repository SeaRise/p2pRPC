package com.rpc.example.server;

import com.rpc.server.RpcService;
import com.rpc.example.api.HelloService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }
}
