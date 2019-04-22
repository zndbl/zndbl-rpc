package com.zndbl.rpc.net.netty;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.net.common.ZndblRpcRequest;
import com.zndbl.rpc.net.common.ZndblRpcResponse;
import com.zndbl.rpc.provider.spring.ZndblRpcSrpringProvider;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<ZndblRpcRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    private ThreadPoolExecutor threadPoolExecutor;
    private ZndblRpcSrpringProvider zndblRpcSrpringProvider;

    public NettyServerHandler(final ThreadPoolExecutor threadPoolExecutor, ZndblRpcSrpringProvider zndblRpcSrpringProvider) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.zndblRpcSrpringProvider = zndblRpcSrpringProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ZndblRpcRequest zndblRpcRequest) throws Exception {
        try {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    ctx.writeAndFlush(zndblRpcSrpringProvider.invokeService(zndblRpcRequest));
                }
            });
        } catch (Exception e) {
            ZndblRpcResponse zndblRpcResponse = new ZndblRpcResponse();
            zndblRpcResponse.setRequestId(zndblRpcRequest.getRequestId());
            zndblRpcResponse.setErrorMsg(com.zndbl.rpc.util.ThrowableUtil.toString(e));
            ctx.writeAndFlush(zndblRpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(">>>>>> zndbl-rpc provider netty server caught exception", cause);
        ctx.fireExceptionCaught(cause);
    }
}