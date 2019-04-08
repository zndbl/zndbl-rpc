package com.zndbl.rpc.remoting.net.impl.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.remoting.invoker.ZndblRpcInvokerFactory;
import com.zndbl.rpc.remoting.net.params.ZndblRpcResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<ZndblRpcResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    private ZndblRpcInvokerFactory zndblRpcInvokerFactory;

    public NettyClientHandler(final ZndblRpcInvokerFactory zndblRpcInvokerFactory) {
        this.zndblRpcInvokerFactory = zndblRpcInvokerFactory;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ZndblRpcResponse zndblRpcResponse) throws Exception {
        zndblRpcInvokerFactory.notifyInvokerFuture(zndblRpcResponse.getRequestId(), zndblRpcResponse);
    }
}