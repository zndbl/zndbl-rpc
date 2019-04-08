package com.zndbl.rpc.remoting.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.remoting.invoker.reference.ZndblRpcRefrenceBean;
import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;

/**
 * @author LANWENJIAN
 * @Date 2019/4/4
 */
public abstract class Client {

    protected static final Logger LOG = LoggerFactory.getLogger(Client.class);

    protected volatile ZndblRpcRefrenceBean zndblRpcRefrenceBean;

    public void init(ZndblRpcRefrenceBean zndblRpcRefrenceBean) {
        this.zndblRpcRefrenceBean = zndblRpcRefrenceBean;
    }

    public abstract void ayncSend(String address, ZndblRpcRequest zndblRpcRequest) throws Exception;
}