package com.rpc.example.client;

import com.rpc.client.RpcProxy;
import com.rpc.example.api.HelloService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient {

    public static void main(String[] args) throws Exception {

        RpcProxy rpcProxy = new ClassPathXmlApplicationContext("clientSpring.xml").getBean(RpcProxy.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("World");
        System.out.println("start");
        System.out.println(result);
        System.exit(0);
    }
}
