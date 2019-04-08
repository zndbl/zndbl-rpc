package com.zndbl.rpc.remoting.invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.registry.ServiceRegistry;
import com.zndbl.rpc.remoting.net.params.BaseCallback;
import com.zndbl.rpc.remoting.net.params.ZndblRpcFutureReponse;
import com.zndbl.rpc.remoting.net.params.ZndblRpcResponse;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * @author zndbl
 * @Date 2019/4/4
 */
public class ZndblRpcInvokerFactory {

    private static Logger LOG = LoggerFactory.getLogger(ZndblRpcInvokerFactory.class);

    private static volatile ZndblRpcInvokerFactory instance = new ZndblRpcInvokerFactory();

    public static ZndblRpcInvokerFactory getInstance() {
        return instance;
    }

    private Class<? extends ServiceRegistry> serviceRegistryClass;
    private Map<String, String> serviceRegistryParam;
    private ServiceRegistry serviceRegistry;
    private List<BaseCallback> stopCallbacklist = new ArrayList<>();
    private ConcurrentHashMap<String, ZndblRpcFutureReponse> futureReponsePool = new ConcurrentHashMap<>();
    private ThreadPoolExecutor responseCallbackThreadPool = null;

    public ZndblRpcInvokerFactory() {
    }

    public ZndblRpcInvokerFactory(Class<? extends ServiceRegistry> serviceRegistryClass,
                                  Map<String, String> serviceRegistryParam) {
        this.serviceRegistryClass = serviceRegistryClass;
        this.serviceRegistryParam = serviceRegistryParam;
    }

    public Class<? extends ServiceRegistry> getServiceRegistryClass() {
        return serviceRegistryClass;
    }

    public void setServiceRegistryClass(Class<? extends ServiceRegistry> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public Map<String, String> getServiceRegistryParam() {
        return serviceRegistryParam;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public List<BaseCallback> getStopCallbacklist() {
        return stopCallbacklist;
    }

    public void setStopCallbacklist(List<BaseCallback> stopCallbacklist) {
        this.stopCallbacklist = stopCallbacklist;
    }

    public ConcurrentHashMap<String, ZndblRpcFutureReponse> getFutureReponsePool() {
        return futureReponsePool;
    }

    public void setFutureReponsePool(ConcurrentHashMap<String, ZndblRpcFutureReponse> futureReponsePool) {
        this.futureReponsePool = futureReponsePool;
    }

    public void start() throws Exception {
        if (serviceRegistryClass != null) {
            serviceRegistry = serviceRegistryClass.newInstance();
            serviceRegistry.start(serviceRegistryParam);
        }
    }

    public void stop() throws Exception {
        if (serviceRegistry != null) {
            serviceRegistry.stop();
        }

        if (stopCallbacklist.size() > 0) {
            for (BaseCallback callback : stopCallbacklist) {
                try {
                    callback.run();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        stopCallbackThreadPool();
    }

    public void stopCallbackThreadPool() {
        if (responseCallbackThreadPool != null) {
            responseCallbackThreadPool.shutdown();
        }
    }

    public void addStopCallBack(BaseCallback callback) {
        stopCallbacklist.add(callback);
    }

    public void setInvokeFuture(String requestId, ZndblRpcFutureReponse futureReponse) {
        futureReponsePool.put(requestId, futureReponse);
    }

    public void removeInvokerFuture(String requestId) {
        futureReponsePool.remove(requestId);
    }

    public void notifyInvokerFuture(String requestId, final ZndblRpcResponse zndblRpcResponse) {
        final ZndblRpcFutureReponse futureReponse = futureReponsePool.get(requestId);
        if (futureReponse == null) {
            return;
        }

        if (futureReponse.getInvokeCallback() != null) {
            try {
                executeResponseCallback(new Runnable() {
                    @Override
                    public void run() {
                        if (zndblRpcResponse.getErrorMsg() != null) {
                            futureReponse.getInvokeCallback().onFailure(new ZndblRpcException(zndblRpcResponse.getErrorMsg()));
                        } else {
                            futureReponse.getInvokeCallback().onSuccess(zndblRpcResponse.getResult());
                        }
                    }
                });
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            futureReponse.setResponse(zndblRpcResponse);
        }

        futureReponsePool.remove(requestId);
    }

    public void executeResponseCallback(Runnable runnable) {
        if (responseCallbackThreadPool == null) {
            synchronized (this) {
                if (responseCallbackThreadPool == null) {
                    responseCallbackThreadPool = new ThreadPoolExecutor(10,
                            100,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(1000),
                            new ThreadFactory() {
                                @Override
                                public Thread newThread(Runnable r) {
                                    return new Thread(r, "xxl-rpc, XxlRpcInvokerFactory-responseCallbackThreadPool-" + r.hashCode());
                                }
                            },
                            new RejectedExecutionHandler() {
                                @Override
                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                    throw new ZndblRpcException("xxl-rpc Invoke Callback Thread pool is EXHAUSTED!");
                                }
                            }
                    );
                }
            }
        }
        responseCallbackThreadPool.execute(runnable);
    }


}