package com.zndbl.rpc.util;

/**
 * @author zndbl
 * @Date 2019/4/3
 */
public class ZndblRpcException extends RuntimeException {

    private static final long serialVersionUID = 42L;

    public ZndblRpcException(String msg) {
        super(msg);
    }

    public ZndblRpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ZndblRpcException(Throwable cause) {
        super(cause);
    }
}