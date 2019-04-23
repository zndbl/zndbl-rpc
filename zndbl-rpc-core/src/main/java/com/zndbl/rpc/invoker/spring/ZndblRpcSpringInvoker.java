package com.zndbl.rpc.invoker.spring;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.ReflectionUtils;

import com.zndbl.rpc.invoker.annotation.ZndblRpcRefrence;
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
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class ZndblRpcSpringInvoker extends InstantiationAwareBeanPostProcessorAdapter implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(ZndblRpcSpringInvoker.class);

    private Class<? extends ServiceRegistry> serviceRegistryClass;

    private ServiceRegistry serviceRegistry;

    private String registryAddress;

    private Class<? extends Client> clientClass;

    private Client client;

    public Class<? extends ServiceRegistry> getServiceRegistryClass() {
        return serviceRegistryClass;
    }

    public void setServiceRegistryClass(Class<? extends ServiceRegistry> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public Class<? extends Client> getClientClass() {
        return clientClass;
    }

    public void setClientClass(Class<? extends Client> clientClass) {
        this.clientClass = clientClass;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (serviceRegistry == null) {
            serviceRegistry = serviceRegistryClass.newInstance();
            serviceRegistry.connectZookeeper(registryAddress);
        }
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(Field field) throws IllegalAccessException {
                if (field.isAnnotationPresent(ZndblRpcRefrence.class)) {
                    Class iface = field.getType();
                    if (!iface.isInterface()) {
                        throw new ZndblRpcException("xxl-rpc, reference(XxlRpcReference) must be interface.");
                    }
                    Object serviceProxy = getObject(iface);
                    field.setAccessible(true);
                    field.set(bean, serviceProxy);
                }
            }
        });

        return super.postProcessAfterInstantiation(bean, beanName);
    }

    private Object getObject(Class iface) {

        Object object = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iface},
                new InvocationHandler() {
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

                        if (client == null) {
                            client = clientClass.newInstance();
                        }
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
                });

        return object;
    }
}