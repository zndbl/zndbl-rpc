package com.zndbl.rpc.serialize.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.zndbl.rpc.serialize.Serializer;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * @author zndbl
 * @Date 2019/4/3
 */
public class HessianSerializer extends Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(os);
        try {
            ho.writeObject(obj);
            ho.flush();
            byte[] result = os.toByteArray();
            return result;
        } catch (IOException e) {
            throw new ZndblRpcException(e);
        } finally {
            try {
                ho.close();
            } catch (IOException e) {
                throw new ZndblRpcException(e);
            }
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Hessian2Input hi = new Hessian2Input(is);
        try {
            Object result = hi.readObject();
            return result;
        } catch (IOException e) {
            throw new ZndblRpcException(e);
        } finally {
            try {
                hi.close();
            } catch (IOException e) {
                throw new ZndblRpcException(e);
            }
        }
    }
}