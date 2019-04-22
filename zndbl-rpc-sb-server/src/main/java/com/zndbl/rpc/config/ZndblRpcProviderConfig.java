package com.zndbl.rpc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zndbl.rpc.net.netty.NettyServer;
import com.zndbl.rpc.provider.spring.ZndblRpcSrpringProvider;
import com.zndbl.rpc.registry.impl.ZkServiceRegistry;
import com.zndbl.rpc.util.IpUtil;

/**
 *
 * @author zndbl
 * @Date 2019/4/3
 */
@Configuration
public class ZndblRpcProviderConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ZndblRpcProviderConfig.class);

    @Value("${zndbl-rpc.application.name}")
    private String applicationName;

    @Value("${zndbl-rpc.remoting.port}")
    private int port;

    @Value("${zndbl-rpc.registry.address}")
    private String registryAddress;

    @Bean
    public ZndblRpcSrpringProvider zndblRpcSpringProviderFactory() {
        ZndblRpcSrpringProvider zndblRpcSrpringProvider = new ZndblRpcSrpringProvider();
        zndblRpcSrpringProvider.setRegistryAddress(registryAddress);
        String ip = IpUtil.getIp();
        zndblRpcSrpringProvider.setServiceAddress(ip + ':' + port);
        zndblRpcSrpringProvider.setApplicationName(applicationName);
        zndblRpcSrpringProvider.setServiceRegistryClass(ZkServiceRegistry.class);
        zndblRpcSrpringProvider.setServerClass(NettyServer.class);
        return zndblRpcSrpringProvider;
    }
}