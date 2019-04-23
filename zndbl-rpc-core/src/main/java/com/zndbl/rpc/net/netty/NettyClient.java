package com.zndbl.rpc.net.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.net.common.ZndblRpcRequest;
import com.zndbl.rpc.net.common.ZndblRpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
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

    private static final Logger LOG = LoggerFactory.getLogger(NettyClient.class);

    private Channel channel;

    @Override
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void asyncSend(String address) {
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
                });

        try {
            final ChannelFuture future = bootstrap.connect(ip, port).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (future.isSuccess()) {
                        LOG.info("连接服务器成功");
                    } else {
                        LOG.info("连接服务器失败");
                    }
                }
            });

            this.channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}