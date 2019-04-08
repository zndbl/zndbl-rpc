package com.zndbl.rpc.remoting.net.impl.netty.client;


import com.zndbl.rpc.remoting.invoker.ZndblRpcInvokerFactory;
import com.zndbl.rpc.remoting.net.common.ConnectClient;
import com.zndbl.rpc.remoting.net.impl.netty.codec.NettyDecoder;
import com.zndbl.rpc.remoting.net.impl.netty.codec.NettyEncoder;
import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;
import com.zndbl.rpc.serialize.Serializer;
import com.zndbl.rpc.util.IpUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * netty pooled client
 *
 * @author xuxueli
 */
public class NettyConnectClient extends ConnectClient {


    private EventLoopGroup group;
    private Channel channel;


    @Override
    public void init(String address, final Serializer serializer, final ZndblRpcInvokerFactory xxlRpcInvokerFactory) throws Exception {

        Object[] array = IpUtil.parseIpPort(address);
        String host = (String) array[0];
        int port = (int) array[1];


        this.group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new NettyEncoder(ZndblRpcRequest.class, serializer))
                                .addLast(new NettyDecoder(ZndblRpcRequest.class, serializer))
                                .addLast(new NettyClientHandler(xxlRpcInvokerFactory));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        this.channel = bootstrap.connect(host, port).sync().channel();

        // valid
        if (!isValidate()) {
            close();
            return;
        }

        LOG.debug(">>>>>>>>>>> xxl-rpc netty client proxy, connect to server success at host:{}, port:{}", host, port);
    }


    @Override
    public boolean isValidate() {
        if (this.channel != null) {
            return this.channel.isActive();
        }
        return false;
    }

    @Override
    public void close() {
        if (this.channel != null && this.channel.isActive()) {
            this.channel.close();        // if this.channel.isOpen()
        }
        if (this.group != null && !this.group.isShutdown()) {
            this.group.shutdownGracefully();
        }
        LOG.debug(">>>>>>>>>>> xxl-rpc netty client close.");
    }


    @Override
    public void send(ZndblRpcRequest zndblRpcRequest) throws Exception {
        this.channel.writeAndFlush(zndblRpcRequest).sync();
    }
}
