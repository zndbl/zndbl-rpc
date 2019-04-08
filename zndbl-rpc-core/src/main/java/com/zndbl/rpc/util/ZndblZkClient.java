package com.zndbl.rpc.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zndbl
 * @Date 2019/4/3
 */
public class ZndblZkClient {

    private static final Logger LOG = LoggerFactory.getLogger(ZndblZkClient.class);

    private String zkaddress;
    private String zkpath;
    private String zkdigest;
    private Watcher watcher;

    public ZndblZkClient(String zkaddress, String zkpath, String zkdigest, Watcher watcher) {
        this.zkaddress = zkaddress;
        this.zkpath = zkpath;
        this.zkdigest = zkdigest;
        this.watcher = watcher;

        if (this.watcher == null) {
            this.watcher = new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    LOG.info(">>>>>>>>> zndbl-rpc:watcher{}", event);

                    if (event.getState() == Event.KeeperState.Expired) {
                        destroy();
                        getClient();
                    }
                }
            };
        }
    }

    private ZooKeeper zooKeeper;
    private ReentrantLock INSTANCE_INTI_LOCK = new ReentrantLock(true);

    public ZooKeeper getClient() {
        if (zooKeeper == null) {
            try {
                if (INSTANCE_INTI_LOCK.tryLock(2, TimeUnit.SECONDS)) {
                    ZooKeeper newZk = null;
                    try {
                        if (zooKeeper == null) {
                            newZk = new ZooKeeper(zkaddress, 10000, watcher);
                            if (zkdigest != null && zkdigest.trim().length() > 0) {
                                newZk.addAuthInfo("digest", zkdigest.getBytes());
                            }
                            newZk.exists(zkpath, false);

                            zooKeeper = newZk;
                            LOG.info(">>>>>>>> zndbl-rpc, ZndblZkClient init success");
                        }
                    } catch (Exception e) {
                        if (newZk != null) {
                            newZk.close();
                        }

                        LOG.error(e.getMessage(), e);
                    } finally {
                        INSTANCE_INTI_LOCK.unlock();
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (zooKeeper == null) {
            throw new ZndblRpcException("ZndblZkClient.zookeeper is null");
        }
        return zooKeeper;
    }

    public void destroy() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
                zooKeeper = null;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private Stat createPathWithParent(String path, boolean watch) {
        if (path == null || path.trim().length() == 0) {
            return null;
        }

        try {
            Stat stat = getClient().exists(path, watch);
            if (stat == null) {
                if (path.lastIndexOf("/") > 0) {
                    String parentPath = path.substring(0, path.lastIndexOf("/"));
                    Stat parentStat = getClient().exists(parentPath, watch);
                    if (parentStat == null) {
                        createPathWithParent(parentPath, false);
                    }
                }

                getClient().create(path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            return getClient().exists(path, true);
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        }
    }

    public void deletePath(String path, boolean watch) {
        try {
            Stat stat = getClient().exists(path, watch);
            if (stat != null) {
                getClient().delete(path, stat.getVersion());
            } else {
                LOG.info(">>>>>>>>> zookeeper node path not found : {]", path);
            }
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        }
    }

    public Stat setPathData(String path, String data, boolean watch) {
        try {
            Stat stat = getClient().exists(path, watch);
            if (stat == null) {
                createPathWithParent(path, watch);
                stat = getClient().exists(path, watch);
            }
            return getClient().setData(path, data.getBytes("UTF-8"), stat.getVersion());
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        }
    }

    public String getPathData(String path, boolean watch) {
        try {
            String znodeValue = null;
            Stat stat = getClient().exists(path, watch);
            if (stat != null) {
                byte[] resultData = getClient().getData(path, watch, null);
                if (resultData != null) {
                    znodeValue = new String(resultData, "UTF-8");
                }
            } else {
                LOG.info(">>>>>>>>> zndbl-rpc, path{} not found", path);
            }
            return znodeValue;
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        }
    }

    public void setChildPathData(String path, String childNode, String childNodeData) {
        try {
            createPathWithParent(path, false);

            String childNodePath = path.concat("/").concat(childNode);

            Stat stat = getClient().exists(childNodePath, false);
            if (stat != null) {
                if (stat.getEphemeralOwner() == 0) {
                    getClient().delete(childNodePath, stat.getVersion());
                } else {
                    return;
                }

                getClient().create(childNodePath, childNodeData.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            }
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        }
    }

    public void deleteChildPath(String path, String childNode) {
        String childNodePath = path.concat("/").concat(childNode);
        deletePath(childNodePath, false);
    }

    public Map<String, String> getChildPathData(String path) {
        HashMap<String, String> allData = new HashMap<>();
        try {
            Stat stat = getClient().exists(path, true);
            if (stat == null) {
                return allData;
            }

            List<String> childNodes = getClient().getChildren(path, true);
            if (childNodes != null && childNodes.size() > 0) {
                for (String childNode : childNodes) {
                    String childNodePath = path.concat("/").concat(childNode);
                    String childNodeValue = getPathData(childNodePath, false);
                    allData.put(childNode, childNodeValue);
                }
            }
            return allData;
        } catch (Exception e) {
            throw new ZndblRpcException(e);
        }
    }


}