package com.zndbl.rpc.remoting.net.params;

import java.io.Serializable;

/**
 * @author LANWENJIAN
 * @Date 2019/4/4
 */
public class ZndblRpcResponse implements Serializable {

    private static final long serialVersioniUID = 42L;

    private String requestId;
    private String errorMsg;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "XxlRpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", result=" + result +
                '}';
    }
}