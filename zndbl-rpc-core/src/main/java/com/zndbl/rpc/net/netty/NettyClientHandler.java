package com.zndbl.rpc.net.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.net.common.ZndblRpcResponse;
import com.zndbl.rpc.util.MapUtil;

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
public class NettyClientHandler extends SimpleChannelInboundHandler<ZndblRpcResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    public NettyClientHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(">>>>>>>>>>> zndbl-rpc netty client caught exception", cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ZndblRpcResponse zndblRpcResponse) throws Exception {
        LOG.info("获取服务端返回信息" + zndblRpcResponse.toString());
        MapUtil.putResponse(zndblRpcResponse.getRequestId(), zndblRpcResponse);
    }
}