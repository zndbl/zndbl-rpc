package com.zndbl.rpc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zndbl.rpc.invoker.spring.ZndblRpcSpringInvoker;
import com.zndbl.rpc.net.netty.NettyClient;
import com.zndbl.rpc.registry.impl.ZkServiceRegistry;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
@Configuration
public class ZndblRpcInvokerConfig {

    @Value("${zndbl-rpc.registry.address}")
    private String registryAddress;

    @Bean
    public ZndblRpcSpringInvoker zndblJobExecutor() {
        ZndblRpcSpringInvoker zndblRpcSpringInvoker = new ZndblRpcSpringInvoker();
        zndblRpcSpringInvoker.setServiceRegistryClass(ZkServiceRegistry.class);
        zndblRpcSpringInvoker.setClientClass(NettyClient.class);
        zndblRpcSpringInvoker.setRegistryAddress(registryAddress);
        return zndblRpcSpringInvoker;
    }
}