package com.zndbl.rpc.remoting.net.impl.netty.server;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;
import com.zndbl.rpc.remoting.net.params.ZndblRpcResponse;
import com.zndbl.rpc.remoting.provider.ZndblRpcProviderFactory;
import com.zndbl.rpc.util.ThrowableUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author zndbl
 * @Date 2019/4/4
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<ZndblRpcRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    private ZndblRpcProviderFactory zndblRpcProviderFactory;
    private ThreadPoolExecutor serverHandlerPool;

    public NettyServerHandler(final ZndblRpcProviderFactory zndblRpcProviderFactory,
                              final ThreadPoolExecutor threadPoolExecutor) {
        this.zndblRpcProviderFactory = zndblRpcProviderFactory;
        this.serverHandlerPool = threadPoolExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ZndblRpcRequest zndblRpcRequest) throws Exception {
        try {
            serverHandlerPool.execute(() -> {
                ZndblRpcResponse zndblRpcResponse = zndblRpcProviderFactory.invokeService(zndblRpcRequest);
                channelHandlerContext.writeAndFlush(zndblRpcResponse);
            });
        } catch (Exception e) {
            ZndblRpcResponse zndblRpcResponse = new ZndblRpcResponse();
            zndblRpcResponse.setRequestId(zndblRpcRequest.getRequestId());
            zndblRpcResponse.setErrorMsg(ThrowableUtil.toString(e));

            channelHandlerContext.writeAndFlush(zndblRpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error(">>>>>>>>> zndbl-rpc provider netty server ca");
        ctx.close();
    }
}