package com.zndbl.rpc.registry.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.registry.ServiceRegistry;
import com.zndbl.rpc.util.ZndblRpcException;
import com.zndbl.rpc.util.ZndblZkClient;

/**
 * @author zndbl
 * @Date 2019/4/3
 */
public class ZkServiceRegistry extends ServiceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(ZkServiceRegistry.class);

    public static final String ENV = "env";
    public static final String ZK_ADDRESS = "zkaddress";
    public static final String ZK_DIGEST = "zkdigest";

    private static final String zkBasePath = "/xxl-rpc";
    private String zkEnvPath;
    private ZndblZkClient zndblZkClient = null;

    private Thread refreshThread;
    private volatile boolean refreshThreadStop = false;

    private volatile ConcurrentHashMap<String, TreeSet<String>> registryData = new ConcurrentHashMap<>();
    private volatile ConcurrentHashMap<String, TreeSet<String>> discoveryData = new ConcurrentHashMap<>();

    public String keyToPath(String nodekey) {
        return zkEnvPath + "/" + nodekey;
    }

    public String pathToKey(String nodePath) {
        if (nodePath == null || nodePath.length() < zkEnvPath.length() || !nodePath.startsWith(zkEnvPath)) {
            return null;
        }
        return nodePath.substring(zkEnvPath.length() + 1, nodePath.length());
    }

    @Override
    public void start(Map<String, String> param) {
        String zkaddress = param.get(ZK_ADDRESS);
        String zkdiggest = param.get(ZK_DIGEST);
        String env = param.get(ENV);

        if (zkaddress == null || zkaddress.trim().length() == 0) {
            throw new ZndblRpcException("zndbl-rpc zkaddress can not be empty");
        }

        if (env == null || env.trim().length() == 0) {
            throw new ZndblRpcException("xxl-rpc env can not be empty");
        }

        zkEnvPath = zkBasePath.concat("/").concat(env);

        zndblZkClient = new ZndblZkClient(zkaddress, zkEnvPath, zkdiggest, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    if (event.getState() == Event.KeeperState.Expired) {
                        zndblZkClient.destroy();
                        zndblZkClient.getClient();
                        refreshDiscoveryData(null);
                    }

                    String path = event.getPath();
                    String key = pathToKey(path);
                    if (key != null) {
                        zndblZkClient.getClient().exists(path, true);
                        if (event.getType() == Event.EventType.NodeChildrenChanged) {
                            refreshDiscoveryData(key);
                        } else if (event.getState() == Event.KeeperState.SyncConnected) {
                            LOG.info("reload all 111");
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });

        zndblZkClient.getClient();

        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!refreshThreadStop) {
                    try {
                        TimeUnit.SECONDS.sleep(60);

                        refreshDiscoveryData(null);

                        refreshRegistryData();
                    } catch (Exception e) {
                        if (!refreshThreadStop) {
                            LOG.error(">>>>>>>>> zndbl-rpc, refresh thread error", e);
                        }
                    }
                }
                LOG.info(">>>>>>>>> zndbl-rpc, refresh thread stopped");
            }
        });

        refreshThread.setName("zndbl-rpc, ZkServiceRegistry refresh thread");
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    @Override
    public void stop() {
        if (zndblZkClient != null) {
            zndblZkClient.destroy();
        }

        if (refreshThread != null) {
            refreshThreadStop = true;
            refreshThread.interrupt();
        }
    }

    @Override
    public boolean registry(Set<String> keys, String value) {
        for (String key : keys) {
            TreeSet<String> values = registryData.get(key);
            if (values == null) {
                values = new TreeSet<>();
                registryData.put(key, values);
            }

            values.add(value);

            String path = keyToPath(key);
            zndblZkClient.setChildPathData(path, value, "");
        }

        LOG.info(">>>>>>>>> zndbl-rpc, registry success, keys = {}, value = {}", keys, value);
        return true;
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        for (String key : keys) {
            TreeSet<String> values = discoveryData.get(key);
            if (value != null) {
                values.remove(value);
            }

            String path = keyToPath(key);
            zndblZkClient.deleteChildPath(path, value);

            LOG.info(">>>>>>>>> zndbl-rpc, remove success, keys = {}, value = {}", keys, value);
        }
        return true;
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        if (keys == null || keys.size() > 0) {
            return null;
        }
        Map<String, TreeSet<String>> registryDataTmp = new HashMap<>();
        for (String key : keys) {
            TreeSet<String> valueSetTmp = discovery(key);
            if (valueSetTmp != null) {
                registryDataTmp.put(key, valueSetTmp);
            }
        }

        return registryDataTmp;
    }

    @Override
    public TreeSet<String> discovery(String key) {
        TreeSet<String> value = discoveryData.get(key);
        if (value == null) {
            refreshDiscoveryData(key);
            value = discoveryData.get(key);
        }
        return value;

    }

    private void refreshDiscoveryData(String key) {
        Set<String> keys = new HashSet<>();
        if (key != null && key.trim().length() > 0) {
            keys.add(key);
        } else {
            if (discoveryData.size() > 0) {
                keys.addAll(discoveryData.keySet());
            }
        }

        if (keys.size() > 0) {
            for (String keyItem : keys) {
                String path = keyToPath(keyItem);
                Map<String, String> childPathData = zndblZkClient.getChildPathData(path);

                TreeSet<String> existValues = discoveryData.get(keyItem);
                if (existValues == null) {
                    existValues = new TreeSet<>();
                    discoveryData.put(keyItem, existValues);
                }

                if (childPathData.size() > 0) {
                    existValues.clear();
                    existValues.addAll(childPathData.keySet());
                }
            }
        }
    }

    private void refreshRegistryData() {
        if (registryData.size() > 0) {
            for (Map.Entry<String, TreeSet<String>> item : registryData.entrySet()) {
                String key = item.getKey();
                for (String value : item.getValue()) {
                    String path = keyToPath(key);
                    zndblZkClient.setChildPathData(path, value, "");
                }
            }
        }
    }
}