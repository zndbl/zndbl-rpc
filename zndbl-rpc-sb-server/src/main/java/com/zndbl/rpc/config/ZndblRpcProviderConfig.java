package com.zndbl.rpc.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zndbl.rpc.registry.impl.ZkServiceRegistry;
import com.zndbl.rpc.remoting.provider.impl.ZndblRpcSpringProviderFactory;

/**
 *
 * @author zndbl
 * @Date 2019/4/3
 */
@Configuration
public class ZndblRpcProviderConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ZndblRpcProviderConfig.class);

    @Value("${zndbl-rpc.remoting.port}")
    private int port;

    @Value("${zndbl-rpc.registry.address}")
    private String address;

    @Value("${zndbl-rpc.registry.env}")
    private String env;

    @Bean
    public ZndblRpcSpringProviderFactory zndblRpcSpringProviderFactory() {
        ZndblRpcSpringProviderFactory factory = new ZndblRpcSpringProviderFactory();
        factory.setPort(port);
        factory.setServiceRegistryClass(ZkServiceRegistry.class);
        Map<String, String> map = new HashMap<>();
        map.put(ZkServiceRegistry.ZK_ADDRESS, address);
        map.put(ZkServiceRegistry.ENV, env);
        factory.setServiceRegistryParam(map);

        LOG.info(">>>>>>>>> zndbl-rpc provider config init finish");
        return factory;
    }


}