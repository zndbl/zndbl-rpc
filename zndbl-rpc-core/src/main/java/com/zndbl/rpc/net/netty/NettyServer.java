package com.zndbl.rpc.net.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.net.common.ZndblRpcRequest;
import com.zndbl.rpc.net.common.ZndblRpcResponse;
import com.zndbl.rpc.provider.spring.ZndblRpcSrpringProvider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/19
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyServer implements Server {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServer.class);

    @Override
    public void start(ZndblRpcSrpringProvider zndblRpcSrpringProvider) {
        String[] strArray = zndblRpcSrpringProvider.getServiceAddress().split(":");
        String port = strArray[1];

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new NettyDecoder(ZndblRpcRequest.class))
                                    .addLast(new NettyEncoder(ZndblRpcResponse.class))
                                    .addLast(new NettyServerHandler(zndblRpcSrpringProvider));
                        }
                    });


            ChannelFuture future = bootstrap.bind(Integer.parseInt(port)).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}