package com.zndbl.rpc.registry;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author LANWENJIAN
 * @Date 2019/4/3
 */
public abstract class ServiceRegistry {

    public abstract void start(Map<String, String> param);

    public abstract void stop();

    public abstract boolean registry(Set<String> keys, String value);

    public abstract boolean remove(Set<String> keys, String value);

    public abstract Map<String, TreeSet<String>> discovery(Set<String> keys);

    public abstract TreeSet<String> discovery(String key);
}