package com.zndbl.rpc.provider.spring;

import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.zndbl.rpc.util.ZndblRpcException;
import com.zndbl.rpc.provider.annotation.ZndblRpcService;

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

    private String registryAddress;

    private String applicationName;

    private String serviceName;

    private String serviceAddress;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
            String version = zndblRpcService.version();
            String interfaceName = zndblRpcService.interfaceName();


        }

    }


    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}