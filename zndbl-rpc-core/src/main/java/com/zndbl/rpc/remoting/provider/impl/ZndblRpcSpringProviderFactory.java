package com.zndbl.rpc.remoting.provider.impl;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.zndbl.rpc.registry.ServiceRegistry;
import com.zndbl.rpc.remoting.net.NetEnum;
import com.zndbl.rpc.remoting.provider.ZndblRpcProviderFactory;
import com.zndbl.rpc.remoting.provider.annotation.ZndblRpcService;
import com.zndbl.rpc.serialize.Serializer;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 *
 * @author zndbl
 * @Date 2019/4/3
 */
public class ZndblRpcSpringProviderFactory extends ZndblRpcProviderFactory implements ApplicationContextAware, InitializingBean, DisposableBean {

    private String netType = NetEnum.NETTY.name();
    private String  serialize = Serializer.SerializeEnum.HESSIAN.name();
    private String ip;
    private int port;
    private String accessToken;
    private Map<String, String> serviceRegistryParam;
    private Class<? extends ServiceRegistry> serviceRegistryClass;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getServiceRegistryParam() {
        return serviceRegistryParam;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }

    public Class<? extends ServiceRegistry> getServiceRegistryClass() {
        return serviceRegistryClass;
    }

    public void setServiceRegistryClass(Class<? extends ServiceRegistry> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(ZndblRpcService.class);
        if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                if (serviceBean.getClass().getInterfaces().length == 0) {
                    throw new ZndblRpcException("zndbl-rpc, service(ZndblRpcService) must inherit interface");
                }

                ZndblRpcService zndblRpcService = serviceBean.getClass().getAnnotation(ZndblRpcService.class);
                String iface = serviceBean.getClass().getInterfaces()[0].getName();
                String version = zndblRpcService.version();

                super.addService(iface, version, serviceBean);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        super.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.prepareConfig();
        super.start();
    }

    private void prepareConfig() {
        NetEnum netTypeEunm = NetEnum.autoMatch(netType, null);
        Serializer.SerializeEnum serializeEnum = Serializer.SerializeEnum.match(serialize, null);
        Serializer serializer = serializeEnum != null ? serializeEnum.getSerializer() : null;

        super.initConfig(netTypeEunm, serializer, ip, port, accessToken, serviceRegistryClass, serviceRegistryParam);
    }
}