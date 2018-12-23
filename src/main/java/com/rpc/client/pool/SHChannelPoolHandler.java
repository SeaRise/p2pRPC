package com.rpc.client.pool;

import com.rpc.client.RpcClientHandler;
import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import com.rpc.protocol.RpcDecoder;
import com.rpc.protocol.RpcEncoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class SHChannelPoolHandler implements ChannelPoolHandler {
    /**
     * 使用完channel需要释放才能放入连接池
     */
    @Override
    public void channelReleased(Channel ch) throws Exception {
        // 刷新管道里的数据
        ch.writeAndFlush(Unpooled.EMPTY_BUFFER); //flush掉所有写回的数据
    }

    /**
     * 获取连接池中的channel
     */
    @Override
    public void channelAcquired(Channel ch) throws Exception {

    }

    /**
     * 当channel不足时会创建，但不会超过限制的最大channel数
     */
    @Override
    public void channelCreated(Channel ch) throws Exception {
        NioSocketChannel channel = (NioSocketChannel) ch;
        channel.config().setKeepAlive(true);
        channel.config().setTcpNoDelay(true);
        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        channel.pipeline().addLast(new RpcEncoder(RpcRequest.class)); // 编码 RPC 请求
        channel.pipeline().addLast(new RpcDecoder(RpcResponse.class)); // 解码 RPC 响应
        channel.pipeline().addLast("RpcClientHandler", new RpcClientHandler());//业务逻辑代码
    }
}
