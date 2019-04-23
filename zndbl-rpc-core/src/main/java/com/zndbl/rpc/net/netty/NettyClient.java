package com.zndbl.rpc.net.netty;

import com.zndbl.rpc.net.common.ZndblRpcRequest;
import com.zndbl.rpc.net.common.ZndblRpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyClient implements Client {

    @Override
    public void asyncSend(String address, ZndblRpcRequest zndblRpcRequest) {
        String[] addressArray = address.split(":");
        String ip = addressArray[0];
        int port = Integer.parseInt(addressArray[1]);
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) {
                        channel.pipeline()
                                .addLast(new NettyEncoder(ZndblRpcRequest.class))
                                .addLast(new NettyDecoder(ZndblRpcResponse.class))
                                .addLast(new NettyClientHandler());
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .connect(ip, port);
    }
}