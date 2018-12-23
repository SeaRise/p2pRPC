package com.rpc.example.server;

import com.rpc.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcBootstrap.class);

    public static void main(String[] args) throws Exception {
        LOGGER.debug("start server");
        new ClassPathXmlApplicationContext("serverSpring.xml");
    }
}
