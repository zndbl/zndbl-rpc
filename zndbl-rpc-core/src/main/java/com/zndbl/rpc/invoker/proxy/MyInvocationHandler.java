package com.zndbl.rpc.invoker.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.net.common.ZndblRpcRequest;
import com.zndbl.rpc.net.common.ZndblRpcResponse;
import com.zndbl.rpc.net.netty.Client;
import com.zndbl.rpc.registry.ServiceRegistry;
import com.zndbl.rpc.util.MapUtil;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/23
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class MyInvocationHandler implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MyInvocationHandler.class);

    private ServiceRegistry serviceRegistry;
    private Client client;

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client nettyClient) {
        this.client = nettyClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = method.getParameters();

        ZndblRpcRequest zndblRpcRequest = new ZndblRpcRequest();
        zndblRpcRequest.setRequestId(UUID.randomUUID().toString());
        zndblRpcRequest.setCreateMillisTime(System.currentTimeMillis());
        zndblRpcRequest.setClassName(className);
        zndblRpcRequest.setMethodName(methodName);
        zndblRpcRequest.setParameters(parameters);
        zndblRpcRequest.setParameterTypes(parameterTypes);

        String key = "/zndbl/test/" + className;
        Set<String> set = serviceRegistry.discovery(key);
        List<String> list = new ArrayList<>(set);
        if (list.size() == 0) {
            throw new ZndblRpcException("rpc service is empty");
        }
        String address = list.get(0);

        client.asyncSend(address, zndblRpcRequest);

        boolean hasResult = true;
        ZndblRpcResponse zndblRpcResponse = null;
        while (hasResult) {
            zndblRpcResponse = MapUtil.getResponse(zndblRpcRequest.getRequestId());
            if (zndblRpcResponse != null) {
                hasResult = false;
            }
        }
        LOG.info("返回结果" + zndblRpcResponse.toString());
        if (zndblRpcResponse.getErrorMsg() != null) {
            return null;
        }
        return zndblRpcResponse.getResult();
    }
}