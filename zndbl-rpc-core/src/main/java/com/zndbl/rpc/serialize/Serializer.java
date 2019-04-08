package com.zndbl.rpc.serialize;

import com.zndbl.rpc.serialize.impl.HessianSerializer;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * @author zndbl
 * @Date 2019/4/3
 */
public abstract class Serializer {

    public abstract <T> byte[] serialize(T obj);
    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);

    public enum SerializeEnum {
        HESSIAN(HessianSerializer.class);

        private Class<? extends Serializer> serializerClass;
        private SerializeEnum(Class<? extends Serializer> serializerClass) {
            this.serializerClass = serializerClass;
        }

        public Serializer getSerializer() {
            try {
                return serializerClass.newInstance();
            } catch (Exception e) {
                throw new ZndblRpcException(e);
            }
        }

        public static SerializeEnum match(String name, SerializeEnum defaultSerializer) {
            for (SerializeEnum item : SerializeEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
            return defaultSerializer;
        }
    }




}