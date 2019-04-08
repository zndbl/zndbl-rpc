package com.zndbl.rpc.remoting.invoker.call;

/**
 * @author LANWENJIAN
 * @Date 2019/4/4
 */
public enum CallType {

    SYNC,

    FUTURE,

    CALLBACK,

    ONEWAY;

    public static CallType match(String name, CallType defaultCallType) {
        for (CallType item : CallType.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }

        return defaultCallType;
    }
}