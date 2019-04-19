package com.zndbl.rpc.util;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/19
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
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