package com.zndbl.rpc.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
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
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        } finally {
            try {
                ho.close();
            } catch (IOException e) {
                throw new ZndblRpcException(e);
            }
            try {
                os.close();
            } catch (IOException e) {
                throw new ZndblRpcException(e);
            }
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Hessian2Input hi = new Hessian2Input(is);
        Object result = null;
        try {
            result = hi.readObject();
            return result;
        } catch (IOException e) {
            throw new ZndblRpcException(e);
        } finally {
            try {
                hi.close();
            } catch (Exception e) {
                throw new ZndblRpcException(e);
            }
            try {
                is.close();
            } catch (IOException e) {
                throw new ZndblRpcException(e);
            }
        }
    }
}