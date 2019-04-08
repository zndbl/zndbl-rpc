package com.zndbl.rpc.remoting.invoker.route.impl;

import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.zndbl.rpc.remoting.invoker.route.ZndblRpcLoadBalance;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class ZndblRpcLoadBalanceRoundStrategy extends ZndblRpcLoadBalance {

    private ConcurrentHashMap<String, Integer> routeCountEachJob = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    private int count(String serviceKey) {
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            routeCountEachJob.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        }

        Integer count = routeCountEachJob.get(serviceKey);
        count = (count == null || count > 1000000) ? (new Random().nextInt(100)) : ++count;
        routeCountEachJob.put(serviceKey, count);
        return count;
    }

    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        String[] addressArr = addressSet.toArray(new String[addressSet.size()]);
        String finalAddress = addressArr[count(serviceKey) % addressArr.length];
        return finalAddress;
    }
}