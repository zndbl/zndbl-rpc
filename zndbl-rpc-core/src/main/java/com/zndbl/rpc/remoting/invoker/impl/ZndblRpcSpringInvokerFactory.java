package com.zndbl.rpc.remoting.invoker.impl;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.ReflectionUtils;

import com.zndbl.rpc.registry.ServiceRegistry;
import com.zndbl.rpc.remoting.invoker.ZndblRpcInvokerFactory;
import com.zndbl.rpc.remoting.invoker.annotation.ZndblRpcRefrence;
import com.zndbl.rpc.remoting.invoker.reference.ZndblRpcRefrenceBean;
import com.zndbl.rpc.remoting.provider.ZndblRpcProviderFactory;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/4
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class ZndblRpcSpringInvokerFactory extends InstantiationAwareBeanPostProcessorAdapter implements InitializingBean, DisposableBean, BeanFactoryAware {

    private Logger LOG = LoggerFactory.getLogger(ZndblRpcSpringInvokerFactory.class);

    private Class<? extends ServiceRegistry> serviceRegistryClass;
    private Map<String, String> serviceRegistryParam;

    public Class<? extends ServiceRegistry> getServiceRegistryClass() {
        return serviceRegistryClass;
    }

    public void setServiceRegistryClass(Class<? extends ServiceRegistry> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public Map<String, String> getServiceRegistryParam() {
        return serviceRegistryParam;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }

    private ZndblRpcInvokerFactory zndblRpcInvokerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        zndblRpcInvokerFactory = new ZndblRpcInvokerFactory(serviceRegistryClass, serviceRegistryParam);
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {

        final Set<String> serviceKeyList = new HashSet<>();
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(ZndblRpcRefrence.class)) {
                    Class iface = field.getType();
                    if (!iface.isInterface()) {
                        throw new ZndblRpcException("xxl-rpc, reference(XxlRpcReference) must be interface.");
                    }

                    ZndblRpcRefrence zndblRpcRefrence = field.getAnnotation(ZndblRpcRefrence.class);

                    ZndblRpcRefrenceBean zndblRpcRefrenceBean = new ZndblRpcRefrenceBean(zndblRpcRefrence.netType(),
                            zndblRpcRefrence.serializer().getSerializer(),
                            zndblRpcRefrence.callType(),
                            zndblRpcRefrence.loadBalance(),
                            iface,
                            zndblRpcRefrence.version(),
                            zndblRpcRefrence.timeout(),
                            zndblRpcRefrence.address(),
                            zndblRpcRefrence.accessToken(),
                            null,
                            zndblRpcInvokerFactory);

                    Object serviceProxy = zndblRpcRefrenceBean.getObject();

                    field.setAccessible(true);
                    field.set(bean, serviceProxy);

                    String serviceKey = ZndblRpcProviderFactory.makeServiceKey(iface.getName(), zndblRpcRefrence.version());
                    serviceKeyList.add(serviceKey);
                }
            }
        });

        if (zndblRpcInvokerFactory.getServiceRegistry() != null) {
            try {
                zndblRpcInvokerFactory.getServiceRegistry().discovery(serviceKeyList);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        return super.postProcessAfterInstantiation(bean, beanName);
    }

    @Override
    public void destroy() throws Exception {
        zndblRpcInvokerFactory.stop();
    }

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }}