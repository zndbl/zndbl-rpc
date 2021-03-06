package com.zndbl.rpc.registry.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.registry.ServiceRegistry;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/19
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class ZkServiceRegistry implements ServiceRegistry, Watcher {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistry.class);
    private static CountDownLatch latch = new CountDownLatch(1);
    private static final int SESSION_TIMEOUT = 5000;
    private static final String REGISTRY_PATH = "/zndbl";
    private ZooKeeper zk;

    public ZkServiceRegistry() {};

    @Override
    public void connectZookeeper(String zkServers) {
        try {
            zk = new ZooKeeper(zkServers, SESSION_TIMEOUT, this);
            latch.await();
            LOG.info("connect to zk");
        } catch (Exception e) {
            LOG.error("create zk client failure", e);
        }
    }

    @Override
    public void registry(String applicationName, String serviceName, String serviceAddress) {
        try {
            String registryPath = REGISTRY_PATH;
            if (zk.exists(registryPath, false) == null) {
                zk.create(registryPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOG.info("create registry node:{}", registryPath);
            }

            String applicationPath = registryPath + "/" + applicationName;
            if (zk.exists(applicationPath, false) == null) {
                zk.create(applicationPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOG.info("create application node:{}", applicationPath);
            }

            String servicePath = applicationPath + "/" + serviceName;
            if (zk.exists(servicePath, false) == null) {
                zk.create(servicePath, serviceAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOG.info("create servicePath node:{}", servicePath);
            }
        } catch (Exception e) {
            LOG.error("create node failure", e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            latch.countDown();
        }
    }

    @Override
    public Set<String> discovery(String key) {
        Set<String> set = new HashSet<>();
        try {
            byte[] bytes = zk.getData(key, true, new Stat());
            String str = new String(bytes);
            set.add(str);
        } catch (Exception e) {
            ZooKeeper.States state = zk.getState();
            LOG.error("获取zk服务异常 {}", e);
        }
        return set;
    }
}