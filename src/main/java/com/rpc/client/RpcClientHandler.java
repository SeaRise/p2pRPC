package com.rpc.client;

import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private ConcurrentHashMap<String, RPCFuture> pendingRPC = new ConcurrentHashMap<String, RPCFuture>();

    private volatile Channel channel;
    private SocketAddress remotePeer;

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        String requestId = response.getRequestId();
        RPCFuture rpcFuture = pendingRPC.get(requestId);
        if (rpcFuture != null) {
            pendingRPC.remove(requestId);
            if (!rpcFuture.isDone()) {
                rpcFuture.done(response);
            }
        }
    }

    /**Netty的I/O异常或handler异常
     * channel发生异常时,关闭所有与channel关联的pending future
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("client caught exception", cause);
        RpcResponse error = new RpcResponse();
        error.setException(new RuntimeException("client caught exception", cause));
        Iterator<RPCFuture> iter = pendingRPC.values().iterator();
        while (iter.hasNext()) {
            RPCFuture rpcFuture = iter.next();
            if (!rpcFuture.isDone()) {
                rpcFuture.done(error);
            }
        }
        pendingRPC.clear();
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendRequest(RpcRequest request, RPCFuture rpcFuture) {
        pendingRPC.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request);
    }
}
