package com.zndbl.rpc.net.netty;

import java.util.List;

import com.zndbl.rpc.serializer.HessianSerializer;
import com.zndbl.rpc.serializer.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public NettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Serializer serializer = new HessianSerializer();
        Object obj = serializer.deserialize(data, genericClass);
        out.add(obj);
    }
}