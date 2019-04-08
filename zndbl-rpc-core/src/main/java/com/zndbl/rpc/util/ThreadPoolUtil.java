package com.zndbl.rpc.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zndbl
 * @Date 2019/4/4
 */
public class ThreadPoolUtil {

    public static ThreadPoolExecutor makeServerThreadPool(final String serverType) {
        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(60,
                300,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "xxl-rpc, " + serverType + "-serverHandlerPool-" + r.hashCode());

            }
        }, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                throw new ZndblRpcException("zndbl-rpc" + serverType + "Thread pool is EXHUSTED!");
            }
        });

        return serverHandlerPool;
    }
}