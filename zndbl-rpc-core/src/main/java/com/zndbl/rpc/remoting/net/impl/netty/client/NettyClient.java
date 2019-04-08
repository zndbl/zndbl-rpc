package com.zndbl.rpc.remoting.net.impl.netty.client;

import com.zndbl.rpc.remoting.net.Client;
import com.zndbl.rpc.remoting.net.common.ConnectClient;
import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class NettyClient extends Client {

    private Class<? extends ConnectClient> connectClientImpl = NettyConnectClient.class;

    @Override
    public void ayncSend(String address, ZndblRpcRequest zndblRpcRequest) throws Exception {
        ConnectClient.asynvSend(zndblRpcRequest, address, connectClientImpl, zndblRpcRefrenceBean);
    }
}