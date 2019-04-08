package com.zndbl.rpc.remoting.net.common;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.remoting.invoker.ZndblRpcInvokerFactory;
import com.zndbl.rpc.remoting.invoker.reference.ZndblRpcRefrenceBean;
import com.zndbl.rpc.remoting.net.params.BaseCallback;
import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;
import com.zndbl.rpc.serialize.Serializer;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public abstract class ConnectClient {

    protected static transient Logger LOG = LoggerFactory.getLogger(ConnectClient.class);

    public abstract void init(String address, final Serializer serializer, final ZndblRpcInvokerFactory zndblRpcInvokerFactory) throws Exception;

    public abstract void close();

    public abstract boolean isValidate();

    public abstract void send(ZndblRpcRequest zndblRpcRequest) throws Exception;

    public static void asynvSend(ZndblRpcRequest zndblRpcRequest, String address,
                                 Class<? extends ConnectClient> connectClientImpl,
                                 final ZndblRpcRefrenceBean zndblRpcRefrenceBean) throws Exception {
        // client pool	[tips03 : may save 35ms/100invoke if move it to constructor, but it is necessary. cause by ConcurrentHashMap.get]
        ConnectClient clientPool = ConnectClient.getPool(address, connectClientImpl, zndblRpcRefrenceBean);

        try {
            // do invoke
            clientPool.send(zndblRpcRequest);
        } catch (Exception e) {
            throw e;
        }
    }

    private static volatile ConcurrentHashMap<String, ConnectClient> connectClientMap;
    private static volatile ConcurrentHashMap<String, Object> connectClientLockMap = new ConcurrentHashMap<>();

    private static ConnectClient getPool(String address, Class<? extends ConnectClient> connectClientImpl,
                                         final ZndblRpcRefrenceBean zndblRpcRefrenceBean) throws Exception {
        if (connectClientMap == null) {
            synchronized (ConnectClient.class) {
                if (connectClientMap == null) {
                    connectClientMap = new ConcurrentHashMap<>();
                    zndblRpcRefrenceBean.getInvokerFactory().addStopCallBack(new BaseCallback() {
                        @Override
                        public void run() throws Exception {
                            if (connectClientMap.size() > 0) {
                                for (String key : connectClientMap.keySet()) {
                                    ConnectClient clientPool = connectClientMap.get(key);
                                    clientPool.close();
                                }
                                connectClientMap.clear();
                            }
                        }
                    });
                }
            }
        }

        ConnectClient connectClient = connectClientMap.get(address);
        if (connectClient != null && connectClient.isValidate()) {
            return connectClient;
        }

        Object clientLock = connectClientLockMap.get(address);
        if (clientLock == null) {
            connectClientLockMap.putIfAbsent(address, new Object());
            clientLock = connectClientLockMap.get(address);
        }

        synchronized (clientLock) {
            connectClient = connectClientMap.get(address);
            if (connectClient != null && connectClient.isValidate()) {
                return connectClient;
            }

            if (connectClient != null) {
                connectClient.close();
                connectClientMap.remove(address);
            }

            ConnectClient connectClient1 = connectClientImpl.newInstance();
            connectClient1.init(address, zndblRpcRefrenceBean.getSerializer(), zndblRpcRefrenceBean.getInvokerFactory());
            connectClientMap.put(address, connectClient1);

            return connectClient1;
        }

    }
}