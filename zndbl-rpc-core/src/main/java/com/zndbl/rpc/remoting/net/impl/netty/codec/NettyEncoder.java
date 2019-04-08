package com.zndbl.rpc.remoting.net.impl.netty.codec;

import com.zndbl.rpc.serialize.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author LANWENJIAN
 * @Date 2019/4/4
 */
public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;
    private Serializer serializer;

    public NettyEncoder(Class<?> genericClass, final Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            byte[] data = serializer.serialize(o);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}