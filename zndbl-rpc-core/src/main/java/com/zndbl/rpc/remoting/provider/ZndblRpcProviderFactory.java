package com.zndbl.rpc.remoting.provider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.registry.ServiceRegistry;
import com.zndbl.rpc.remoting.net.NetEnum;
import com.zndbl.rpc.remoting.net.Server;
import com.zndbl.rpc.remoting.net.params.BaseCallback;
import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;
import com.zndbl.rpc.remoting.net.params.ZndblRpcResponse;
import com.zndbl.rpc.serialize.Serializer;
import com.zndbl.rpc.util.IpUtil;
import com.zndbl.rpc.util.NetUtil;
import com.zndbl.rpc.util.ThrowableUtil;
import com.zndbl.rpc.util.ZndblRpcException;


/**
 * @author zndbl
 * @Date 2019/4/3
 */
public class ZndblRpcProviderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ZndblRpcProviderFactory.class);

    private NetEnum netType;
    private Serializer serializer;

    private String ip;
    private int port;
    private String accessToken;

    private Class<? extends ServiceRegistry> serviceRegistryClass;
    private Map<String, String> serviceRegistryParam;

    public ZndblRpcProviderFactory() {
    }

    public void initConfig(NetEnum netType, Serializer serializer,
                           String ip, int port,
                           String accessToken, Class<? extends ServiceRegistry> serviceRegistryClass,
                           Map<String, String> serviceRegistryParam) {
        this.netType = netType;
        this.serializer = serializer;
        this.ip = ip;
        this.port = port;
        this.accessToken = accessToken;
        this.serviceRegistryClass = serviceRegistryClass;
        this.serviceRegistryParam = serviceRegistryParam;

        if (this.netType == null) {
            throw new ZndblRpcException("zndbl-rpc provider nettype missing");
        }

        if (this.serializer == null) {
            throw new ZndblRpcException("zndbl-rpc provider serializer missing");
        }

        if (this.ip == null) {
            this.ip = IpUtil.getIp();
        }

        if (this.port <= 0) {
            this.port = 7080;
        }

        if (NetUtil.isPortUsed(this.port)) {
            throw new ZndblRpcException("zndbl-rpc provider port[}" + this.port + "] is used");
        }

        if (this.serviceRegistryClass != null) {
            if (this.serviceRegistryParam == null) {
                throw new ZndblRpcException("zndbl-rpc provider serviceRegistryParam is missing");
            }
        }
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public int getPort() {
        return port;
    }

    private Server server;
    private ServiceRegistry serviceRegistry;
    private String serviceAddress;

    public void start() throws Exception {
        serviceAddress = IpUtil.getIpPort(this.ip, port);
        server = netType.serverClass.newInstance();
        server.setStartedCallback(new BaseCallback() {
            @Override
            public void run() throws Exception {
                if (serviceRegistryClass != null) {
                    serviceRegistry = serviceRegistryClass.newInstance();
                    serviceRegistry.start(serviceRegistryParam);
                    if (serviceData.size() > 0) {
                        serviceRegistry.registry(serviceData.keySet(), serviceAddress);
                    }
                }
            }
        });
        server.setStoppedCallback(new BaseCallback() {
            @Override
            public void run() throws Exception {
                if (serviceData != null) {
                    if (serviceData.size() > 0) {
                        serviceRegistry.remove(serviceData.keySet(), serviceAddress);
                    }
                    serviceRegistry.stop();
                    serviceRegistry = null;
                }
            }
        });
        server.start(this);
    }

    public void stop() throws Exception {
        server.stop();
    }

    private Map<String, Object> serviceData = new HashMap<>();

    public Map<String, Object> getServiceData() {
        return serviceData;
    }

    public static String makeServiceKey(String iface, String version) {
        String serviceKey = iface;
        if (version != null && version.trim().length() > 0) {
            serviceKey += "#".concat(version);
        }
        return serviceKey;
    }

    public void addService(String iface, String version, Object serviceBean) {
        String serviceKey = makeServiceKey(iface, version);
        serviceData.put(serviceKey, serviceBean);
    }

    public ZndblRpcResponse invokeService(ZndblRpcRequest zndblRpcRequest) {
        ZndblRpcResponse zndblRpcResponse = new ZndblRpcResponse();
        zndblRpcResponse.setRequestId(zndblRpcRequest.getRequestId());

        String serviceKey = makeServiceKey(zndblRpcRequest.getClassName(), zndblRpcRequest.getVersion());
        Object serviceBean = serviceData.get(serviceKey);

        if (serviceBean == null) {
            zndblRpcResponse.setErrorMsg("The serviceKey["+ serviceKey +"] not found.");
            return zndblRpcResponse;
        }

        if (System.currentTimeMillis() - zndblRpcRequest.getCreateMillisTime() > 3 * 60 * 1000) {
            zndblRpcResponse.setErrorMsg("The timestamp difference between admin and executor exceeds the limit.");
            return zndblRpcResponse;
        }

        if (accessToken != null && accessToken.trim().length() > 0 && !accessToken.trim().equals(zndblRpcRequest.getAccessToken())) {
            zndblRpcResponse.setErrorMsg("The access token[" + zndblRpcRequest.getAccessToken() + "] is wrong.");
            return zndblRpcResponse;
        }

        try {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = zndblRpcRequest.getMethodName();
            Class<?>[] parameterTypes = zndblRpcRequest.getParameterTypes();
            Object[] parameters = zndblRpcRequest.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(serviceBean, parameters);
        } catch (Throwable t) {
            LOG.error("zndbl-rpc provider invokeService error", t);
            zndblRpcResponse.setErrorMsg(ThrowableUtil.toString(t));
        }

        return zndblRpcResponse;
    }
}