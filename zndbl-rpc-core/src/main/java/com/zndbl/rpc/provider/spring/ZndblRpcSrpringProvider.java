package com.zndbl.rpc.provider.spring;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.zndbl.rpc.net.Server;
import com.zndbl.rpc.provider.annotation.ZndblRpcService;
import com.zndbl.rpc.registry.ServiceRegistry;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/19
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class ZndblRpcSrpringProvider implements ApplicationContextAware, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(ZndblRpcSrpringProvider.class);

    private String registryAddress;

    private String serviceAddress;

    private Class<? extends ServiceRegistry> serviceRegistryClass;

    private ServiceRegistry serviceRegistry;

    private Class<? extends Server> serverClass;

    private Server server;

    public Class<? extends Server> getServerClass() {
        return serverClass;
    }

    public void setServerClass(Class<? extends Server> serverClass) {
        this.serverClass = serverClass;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public Class<? extends ServiceRegistry> getServiceRegistryClass() {
        return serviceRegistryClass;
    }

    public void setServiceRegistryClass(Class<? extends ServiceRegistry> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    // first process
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Map<String, Object> map = applicationContext.getBeansWithAnnotation(ZndblRpcService.class);
        if (map == null || map.size() == 0) {
            return;
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            int length = value.getClass().getInterfaces().length;
            if (length == 0) {
                throw new ZndblRpcException("ZndblRpcService must have interface");
            }
            ZndblRpcService zndblRpcService = value.getClass().getAnnotation(ZndblRpcService.class);
            String group = zndblRpcService.group();
            String interfaceName = zndblRpcService.interfaceName();
            try {
                if (serviceRegistry == null) {
                    serviceRegistry = serviceRegistryClass.newInstance();
                    serviceRegistry.connectZookeeper(registryAddress);
                }
                serviceRegistry.registry(group, interfaceName, serviceAddress);
            } catch (Exception e) {
                LOG.error("init serviceRegistry has throwable", e);
            }
        }
    }


    @Override
    public void destroy() throws Exception {

    }

    // second process
    @Override
    public void afterPropertiesSet() throws Exception {
        if (server == null) {
            server = serverClass.newInstance();
        }


    }
}