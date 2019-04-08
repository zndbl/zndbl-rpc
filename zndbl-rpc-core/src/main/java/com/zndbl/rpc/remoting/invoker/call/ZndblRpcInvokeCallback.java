package com.zndbl.rpc.remoting.invoker.call;

/**
 * @author zndbl
 * @Date 2019/4/4
 */
public abstract class ZndblRpcInvokeCallback<T> {

    public abstract void onSuccess(T result);

    public abstract void onFailure(Throwable exception);

    private static ThreadLocal<ZndblRpcInvokeCallback> threadInvokerFuture = new ThreadLocal<>();

    public static ZndblRpcInvokeCallback getCallback() {
        ZndblRpcInvokeCallback invokeCallback = threadInvokerFuture.get();
        threadInvokerFuture.remove();
        return invokeCallback;
    }

    public static void setCallback(ZndblRpcInvokeCallback invokeCallback) {
        threadInvokerFuture.set(invokeCallback);
    }

    public static void removeCallback() {
        threadInvokerFuture.remove();
    }
}