package com.zndbl.rpc.net.netty;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.net.common.ZndblRpcRequest;
import com.zndbl.rpc.net.common.ZndblRpcResponse;
import com.zndbl.rpc.provider.spring.ZndblRpcSrpringProvider;
import com.zndbl.rpc.util.ThreadPoolUtil;

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

    private Thread thread;

    @Override
    public void start(ZndblRpcSrpringProvider zndblRpcSrpringProvider) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] strArray = zndblRpcSrpringProvider.getServiceAddress().split(":");
                String port = strArray[1];

                ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool();
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel channel) throws Exception {
                                    channel.pipeline()
                                            .addLast(new NettyDecoder(ZndblRpcRequest.class))
                                            .addLast(new NettyEncoder(ZndblRpcResponse.class))
                                            .addLast(new NettyServerHandler(threadPoolExecutor, zndblRpcSrpringProvider));
                                }
                            })
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childOption(ChannelOption.TCP_NODELAY, true);

                    ChannelFuture future = bootstrap.bind(Integer.parseInt(port)).sync();
                    future.channel().closeFuture().sync();
                } catch (Exception e) {

                    if (e instanceof InterruptedException) {
                        LOG.info(">>>>>>>>>>> xxl-rpc remoting server stop.");
                    } else {
                        LOG.error(">>>>>>>>>>> xxl-rpc remoting server error.", e);
                    }
                } finally {

                    try {
                        threadPoolExecutor.shutdown();    // shutdownNow
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                    try {
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        });
        thread.start();
    }
}