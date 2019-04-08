package com.zndbl.rpc.remoting.invoker.call;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.zndbl.rpc.remoting.net.params.ZndblRpcFutureReponse;
import com.zndbl.rpc.remoting.net.params.ZndblRpcResponse;
import com.zndbl.rpc.util.ZndblRpcException;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/4
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class ZndblRpcInvokeFuture implements Future {

    private ZndblRpcFutureReponse futureReponse;

    public ZndblRpcInvokeFuture(ZndblRpcFutureReponse futureReponse) {
        this.futureReponse = futureReponse;
    }

    public void stop() {
        futureReponse.removeInvokerFuture();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureReponse.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return futureReponse.isDone();
    }

    @Override
    public boolean isDone() {
        return futureReponse.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            return get(-1, TimeUnit.MICROSECONDS);
        } catch (TimeoutException e) {
            throw new ZndblRpcException(e);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            ZndblRpcResponse zndblRpcResponse = futureReponse.get(timeout, unit);
            if (zndblRpcResponse.getErrorMsg() != null) {
                throw new ZndblRpcException(zndblRpcResponse.getErrorMsg());
            }
            return zndblRpcResponse.getResult();
        } finally {
            stop();
        }
    }

    private static ThreadLocal<ZndblRpcInvokeFuture> threadInvokerFuture = new ThreadLocal<>();

    public static <T> Future<T> getFuture(Class<T> type) {
        Future<T> future = threadInvokerFuture.get();
        threadInvokerFuture.remove();
        return future;
    }

    public static void setFuture(ZndblRpcInvokeFuture future) {
        threadInvokerFuture.set(future);
    }

    public static void removeFuture() {
        threadInvokerFuture.remove();
    }
}