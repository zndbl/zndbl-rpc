package com.zndbl.rpc.invoker.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.ReflectionUtils;

import com.zndbl.rpc.invoker.annotation.ZndblRpcRefrence;
import com.zndbl.rpc.invoker.proxy.MyInvocationHandler;
import com.zndbl.rpc.net.netty.Client;
import com.zndbl.rpc.registry.ServiceRegistry;
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
        if (client == null) {
            client = clientClass.newInstance();
        }

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
                        throw new ZndblRpcException("zndbl-rpc, reference(ZndblRpcRefrence) must be interface.");
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
        MyInvocationHandler invocationHandler = new MyInvocationHandler();
        invocationHandler.setServiceRegistry(serviceRegistry);
        invocationHandler.setClient(client);

        Object object = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{iface}, invocationHandler);
        return object;
    }
}