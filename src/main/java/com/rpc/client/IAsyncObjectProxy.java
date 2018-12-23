package com.rpc.client;

/**
 * Created by luxiaoxun on 2016/3/16.
 */
public interface IAsyncObjectProxy {
    RPCFuture call(final Class<?> interfaceClass, String funcName, final Object[] args, final Class<?>[] parameterTypes);
}