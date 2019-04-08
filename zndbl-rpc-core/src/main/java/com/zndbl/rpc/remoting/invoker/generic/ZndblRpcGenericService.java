package com.zndbl.rpc.remoting.invoker.generic;

/**
 * @author zndbl
 * @Date 2019/4/4
 */
public interface ZndblRpcGenericService {

    public Object invoke(String iface, String version, String method, String[] parameterTypes, Object[] args);
}