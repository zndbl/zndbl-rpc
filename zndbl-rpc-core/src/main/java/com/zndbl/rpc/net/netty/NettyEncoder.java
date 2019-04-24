package com.zndbl.rpc.net.netty;

import com.alibaba.fastjson.JSON;
import com.zndbl.rpc.net.common.ZndblRpcResponse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;

    public NettyEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {

        if (genericClass.isInstance(in)) {
//            Serializer serializer = new HessianSerializer();
//            byte[] data = serializer.serialize(in);
            ZndblRpcResponse zndblRpcResponse = (ZndblRpcResponse) in;
            byte[] data = JSON.toJSONBytes(zndblRpcResponse);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}