package com.zndbl.rpc.remoting.net.impl.netty.server;

import java.util.concurrent.ThreadPoolExecutor;

import com.zndbl.rpc.remoting.net.Server;
import com.zndbl.rpc.remoting.net.impl.netty.codec.NettyDecoder;
import com.zndbl.rpc.remoting.net.impl.netty.codec.NettyEncoder;
import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;
import com.zndbl.rpc.remoting.provider.ZndblRpcProviderFactory;
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
 * @author zndbl
 * @Date 2019/4/4
 */
public class NettyServer extends Server {

    private Thread thread;

    @Override
    public void start(ZndblRpcProviderFactory zndblRpcProviderFactory) throws Exception {
        thread = new Thread((Runnable) () -> {
            final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(NettyServer.class.getSimpleName());
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
                                        .addLast(new NettyDecoder(ZndblRpcRequest.class, zndblRpcProviderFactory.getSerializer()))
                                        .addLast(new NettyEncoder(ZndblRpcRequest.class, zndblRpcProviderFactory.getSerializer()))
                                        .addLast(new NettyServerHandler(zndblRpcProviderFactory, serverHandlerPool));

                            }

                        })
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = bootstrap.bind(zndblRpcProviderFactory.getPort()).sync();

                onStarted();

                future.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    LOG.info(">>>>>>>> zndbl-rpc remoting server stop");
                } else {
                    LOG.error(">>>>>>>> zndbl-rpc remoting server error", e);
                }
            } finally {
                try {
                    serverHandlerPool.shutdown();
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
        });
    }

    @Override
    public void stop() throws Exception {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }

        onStoped();
        LOG.info(">>>>>>>>> zndbl-rpc remoting server destroy success");
    }

}