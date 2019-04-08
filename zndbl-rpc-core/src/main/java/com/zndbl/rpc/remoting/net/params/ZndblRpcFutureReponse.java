package com.zndbl.rpc.remoting.net.params;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.zndbl.rpc.remoting.invoker.call.ZndblRpcInvokeCallback;
import com.zndbl.rpc.remoting.invoker.ZndblRpcInvokerFactory;
import com.zndbl.rpc.util.ZndblRpcException;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * @author LANWENJIAN
 * @Date 2019/4/4
 */
public class ZndblRpcFutureReponse implements Future<ZndblRpcResponse> {

    private ZndblRpcInvokerFactory invokerFactory;

    private ZndblRpcRequest request;
    private ZndblRpcResponse response;

    private boolean done = false;
    private Object lock = new Object();

    private ZndblRpcInvokeCallback invokeCallback;

    public ZndblRpcFutureReponse(final ZndblRpcInvokerFactory invokerFactory, ZndblRpcRequest request,
                                 ZndblRpcInvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
        this.request = request;
        this.invokerFactory = invokerFactory;

        setInvokeFuture();
    }

    public void setInvokeFuture() {
        this.invokerFactory.setInvokeFuture(request.getRequestId(), this);
    }

    public void removeInvokerFuture() {
        this.invokerFactory.removeInvokerFuture(request.getRequestId());
    }

    public ZndblRpcRequest getRequest() {
        return request;
    }

    public ZndblRpcInvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public void setResponse(ZndblRpcResponse response) {
        this.response = response;
        synchronized (lock) {
            done = true;
            lock.notifyAll();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public ZndblRpcResponse get() throws InterruptedException, ExecutionException {
        try {
            return get(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new ZndblRpcException(e);
        }
    }

    @Override
    public ZndblRpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            synchronized (lock) {
                try {
                    if (timeout < 0) {
                        lock.wait();
                    } else {
                        long timeoutMillis = TimeUnit.MICROSECONDS == unit ? timeout : MICROSECONDS.convert(timeout, unit);
                        lock.wait(timeoutMillis);
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }

        if (!done) {
            throw new ZndblRpcException("xxl-rpc, request timeout at:"+ System.currentTimeMillis() +", request:" + request.toString());
        }
        return response;
    }
}