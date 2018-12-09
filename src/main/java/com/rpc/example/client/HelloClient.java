package com.rpc.example.client;

import com.rpc.client.RpcProxy;
import com.rpc.example.api.HelloService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient {

    public static void main(String[] args) throws Exception {

        RpcProxy rpcProxy = new ClassPathXmlApplicationContext("clientSpring.xml").getBean(RpcProxy.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("World");
        System.out.println(result);

        HelloService helloService2 = rpcProxy.create(HelloService.class, "sample.hello2");
        String result2 = helloService2.hello("世界");
        System.out.println(result2);

        System.exit(0);
    }
}
