package com.zndbl.rpc.remoting.invoker.reference;

import java.lang.reflect.Method;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import com.zndbl.rpc.remoting.invoker.ZndblRpcInvokerFactory;
import com.zndbl.rpc.remoting.invoker.call.CallType;
import com.zndbl.rpc.remoting.invoker.call.ZndblRpcInvokeCallback;
import com.zndbl.rpc.remoting.invoker.call.ZndblRpcInvokeFuture;
import com.zndbl.rpc.remoting.invoker.route.LoadBalance;
import com.zndbl.rpc.remoting.net.Client;
import com.zndbl.rpc.remoting.net.NetEnum;
import com.zndbl.rpc.remoting.net.params.ZndblRpcFutureReponse;
import com.zndbl.rpc.remoting.net.params.ZndblRpcRequest;
import com.zndbl.rpc.remoting.net.params.ZndblRpcResponse;
import com.zndbl.rpc.remoting.provider.ZndblRpcProviderFactory;
import com.zndbl.rpc.serialize.Serializer;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * @author zndbl
 * @Date 2019/4/4
 */
public class ZndblRpcRefrenceBean {

    private static final Logger LOG = LoggerFactory.getLogger(ZndblRpcRefrenceBean.class);

    private NetEnum netType;
    private Serializer serializer;
    private CallType callType;
    private LoadBalance loadBalance;

    private Class<?> iface;
    private String version;

    private long timeout = 1000;

    private String address;
    private String accessToken;

    private ZndblRpcInvokeCallback invokeCallback;

    private ZndblRpcInvokerFactory invokerFactory;

    public ZndblRpcRefrenceBean(NetEnum netType,
                                Serializer serializer,
                                CallType callType,
                                LoadBalance loadBalance,
                                Class<?> iface,
                                String version,
                                long timeout,
                                String address,
                                String accessToken,
                                ZndblRpcInvokeCallback zndblRpcInvokeCallback,
                                ZndblRpcInvokerFactory zndblRpcInvokerFactory) {
        this.netType = netType;
        this.serializer = serializer;
        this.callType = callType;
        this.loadBalance = loadBalance;
        this.iface = iface;
        this.version = version;
        this.timeout = timeout;
        this.address = address;
        this.accessToken = accessToken;
        this.invokeCallback = invokeCallback;
        this.invokerFactory = invokerFactory;

        // valid
        if (this.netType == null) {
            throw new ZndblRpcException("xxl-rpc reference netType missing.");
        }
        if (this.serializer == null) {
            throw new ZndblRpcException("xxl-rpc reference serializer missing.");
        }
        if (this.callType == null) {
            throw new ZndblRpcException("xxl-rpc reference callType missing.");
        }
        if (this.loadBalance == null) {
            throw new ZndblRpcException("xxl-rpc reference loadBalance missing.");
        }
        if (this.iface == null) {
            throw new ZndblRpcException("xxl-rpc reference iface missing.");
        }
        if (this.timeout < 0) {
            this.timeout = 0;
        }
        if (this.invokerFactory == null) {
            this.invokerFactory = ZndblRpcInvokerFactory.getInstance();
        }

        initClient();
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public long getTimeout() {
        return timeout;
    }

    public ZndblRpcInvokerFactory getInvokerFactory() {
        return invokerFactory;
    }

    Client client = null;

    private void initClient() {
        try {
            client = netType.clientClass.newInstance();
            client.init(this);
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        }
    }

    public Class<?> getObjectType() {
        return iface;
    }

    public Object getObject() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                        String className = method.getDeclaringClass().getName();
                        String versions = version;
                        String methodName = method.getName();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] parameters = args;

                        if (className.equals(Object.class.getName())) {
                            throw new ZndblRpcException("zndbl-rpc proxy class-method not support");
                        }

                        String finalAddress = address;
                        if (finalAddress == null || finalAddress.trim().length() == 0) {
                            if (invokerFactory != null && invokerFactory.getServiceRegistry() != null) {
                                String serviceKey = ZndblRpcProviderFactory.makeServiceKey(className, versions);
                                TreeSet<String> addressSet = invokerFactory.getServiceRegistry().discovery(serviceKey);
                                if (addressSet == null || addressSet.size() == 0) {

                                } else if (addressSet.size() == 1) {
                                    finalAddress = addressSet.first();
                                } else {
                                    finalAddress = loadBalance.zndblRpcInvokerRouter.route(serviceKey, addressSet);
                                }

                            }
                        }

                        if (finalAddress == null || finalAddress.trim().length() == 0) {
                            throw new ZndblRpcException("xxl-rpc reference bean[" + className + "] address empty");
                        }

                        ZndblRpcRequest zndblRpcRequest = new ZndblRpcRequest();
                        zndblRpcRequest.setRequestId(UUID.randomUUID().toString());
                        zndblRpcRequest.setCreateMillisTime(System.currentTimeMillis());
                        zndblRpcRequest.setAccessToken(accessToken);
                        zndblRpcRequest.setClassName(className);
                        zndblRpcRequest.setMethodName(methodName);
                        zndblRpcRequest.setParameterTypes(parameterTypes);
                        zndblRpcRequest.setParameters(parameters);

                        if (CallType.SYNC == callType) {
                            ZndblRpcFutureReponse futureReponse = new ZndblRpcFutureReponse(invokerFactory, zndblRpcRequest, null);
                            try {
                                client.ayncSend(finalAddress, zndblRpcRequest);

                                ZndblRpcResponse zndblRpcResponse = futureReponse.get(timeout, TimeUnit.MILLISECONDS);
                                if (zndblRpcResponse.getErrorMsg() != null) {
                                    throw new ZndblRpcException(zndblRpcResponse.getErrorMsg());
                                }
                                return zndblRpcResponse.getResult();
                            } catch (Exception e) {
                                throw (e instanceof ZndblRpcException) ? e : new ZndblRpcException(e);
                            } finally {
                                futureReponse.removeInvokerFuture();
                            }
                        } else if (CallType.FUTURE == callType) {
                            ZndblRpcFutureReponse futureReponse = new ZndblRpcFutureReponse(invokerFactory, zndblRpcRequest, null);

                            try {
                                ZndblRpcInvokeFuture invokeFuture = new ZndblRpcInvokeFuture(futureReponse);
                                ZndblRpcInvokeFuture.setFuture(invokeFuture);

                                client.ayncSend(finalAddress, zndblRpcRequest);
                                return null;
                            } catch (Exception e) {
                                futureReponse.removeInvokerFuture();
                                throw (e instanceof ZndblRpcException) ? e : new ZndblRpcException(e);
                            }
                        } else if (CallType.CALLBACK == callType) {

                            ZndblRpcInvokeCallback finalInvokeCallback = invokeCallback;
                            ZndblRpcInvokeCallback threadInvokeCallback = ZndblRpcInvokeCallback.getCallback();
                            if (threadInvokeCallback != null) {
                                finalInvokeCallback = threadInvokeCallback;
                            }
                            if (finalInvokeCallback == null) {
                                throw new ZndblRpcException("xxl-rpc XxlRpcInvokeCallback（CallType=" + CallType.CALLBACK.name() + "） cannot be null.");
                            }

                            ZndblRpcFutureReponse futureReponse = new ZndblRpcFutureReponse(invokerFactory, zndblRpcRequest, finalInvokeCallback);
                            try {
                                client.ayncSend(finalAddress, zndblRpcRequest);
                            } catch (Exception e) {
                                futureReponse.removeInvokerFuture();
                                throw (e instanceof ZndblRpcException) ? e : new ZndblRpcException(e);
                            }
                            return null;
                        } else if (CallType.ONEWAY == callType) {
                            client.ayncSend(finalAddress, zndblRpcRequest);
                            return null;
                        } else {
                            throw new ZndblRpcException("xxl-rpc callType[" + callType + "] invalid");
                        }

                    }
                });
    }


}