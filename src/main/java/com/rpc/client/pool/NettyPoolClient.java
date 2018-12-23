package com.rpc.client.pool;

import com.rpc.client.RPCFuture;
import com.rpc.client.RpcClientHandler;
import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyPoolClient {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap strap = new Bootstrap();

    private ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    private static class SingletonHolder {
        private static NettyPoolClient singleton = new NettyPoolClient();
    }

    public static NettyPoolClient getInstance() {
        return SingletonHolder.singleton;
    }

    public NettyPoolClient() {
        build();
    }

    private void build() {
        strap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                return new FixedChannelPool(strap.remoteAddress(key), new SHChannelPoolHandler(), 1);
            }
        };
    }

    public RPCFuture send(String host, int port, final RpcRequest request) {
        final SimpleChannelPool pool = poolMap.get(new InetSocketAddress(host, port));
        Future<Channel> f = pool.acquire();
        final RPCFuture rpcFuture = new RPCFuture(request);
        f.addListener(new FutureListener<Channel>() {
            @Override
            public void operationComplete(final Future<Channel> channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    submit(new Runnable() {
                        @Override
                        public void run() {
                            Channel ch = channelFuture.getNow();
                            RpcClientHandler handler = (RpcClientHandler) ch.pipeline().get("RpcClientHandler");
                            handler.sendRequest(request, rpcFuture);
                            pool.release(ch);
                        }
                    });
                } else {
                    RpcResponse error = new RpcResponse();
                    error.setException(new RuntimeException("channel pool acquire exception", channelFuture.cause()));
                    rpcFuture.done(error);
                }
            }
        });
        return rpcFuture;
    }
}
