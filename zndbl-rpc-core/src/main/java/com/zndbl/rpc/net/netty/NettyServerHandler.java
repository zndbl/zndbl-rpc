package com.zndbl.rpc.net.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.net.common.ZndblRpcRequest;
import com.zndbl.rpc.net.common.ZndblRpcResponse;
import com.zndbl.rpc.provider.spring.ZndblRpcSrpringProvider;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServerHandler.class);

    private ZndblRpcSrpringProvider zndblRpcSrpringProvider;

    public NettyServerHandler(ZndblRpcSrpringProvider zndblRpcSrpringProvider) {
        this.zndblRpcSrpringProvider = zndblRpcSrpringProvider;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ZndblRpcRequest zndblRpcRequest = (ZndblRpcRequest) msg;
        try {
            LOG.info("接收到客户端信息:" + zndblRpcRequest.toString());
            ctx.writeAndFlush(zndblRpcSrpringProvider.invokeService(zndblRpcRequest));
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