package com.rpc.client;


import com.rpc.client.pool.NettyPoolClient;
import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import com.rpc.protocol.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 * 同步调用
 */
public class RpcProxy implements IAsyncObjectProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String host;

    private int port;

    public RpcProxy(String serviceAddress) {
        // 从 RPC 服务地址中解析主机名与端口号
        if (StringUtil.isEmpty(serviceAddress)) {
            throw new RuntimeException("server address is empty");
        }
        String[] array = StringUtil.split(serviceAddress, ":");
        this.host = array[0];
        this.port = Integer.parseInt(array[1]);
    }

    public RpcProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object obj = invokeObjectMethod(proxy, method, args);
                        if (obj != null) {
                            return obj;
                        }
                        // 创建 RPC 请求对象并设置请求属性
                        RpcRequest request = createRequest(method, args);
                        // 创建 RPC 客户端对象并发送 RPC 请求
                        RpcResponse response = NettyPoolClient.getInstance().send(host, port, request).get();
                        // 返回 RPC 响应结果
                        if (response.hasException()) {
                            throw response.getException();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }

    /**
     * 本地方法,本地执行
     * */
    private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        return null;
    }

    @Override
    public RPCFuture call(final Class<?> interfaceClass, String funcName, final Object[] args, final Class<?>[] parameterTypes) {
        RpcRequest request = createRequest(interfaceClass, funcName, args, parameterTypes);
        return NettyPoolClient.getInstance().send(host, port, request);
    }

    private RpcRequest createRequest(final Method method, final Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        return request;
    }

    private RpcRequest createRequest(final Class<?> interfaceClass, final String funcName, final Object[] args, final Class<?>[] parameterTypes) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(interfaceClass.getName());
        request.setMethodName(funcName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(args);
        return request;
    }
}