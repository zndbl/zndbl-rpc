package com.zndbl.rpc.remoting.net;

import com.zndbl.rpc.remoting.net.impl.netty.client.NettyClient;
import com.zndbl.rpc.remoting.net.impl.netty.server.NettyServer;

/**
 *
 * @author zndbl
 * @Date 2019/4/3
 */
public enum NetEnum {

    NETTY(NettyServer.class, NettyClient.class);

    public final Class<? extends Server> serverClass;
    public final Class<? extends Client> clientClass;

    NetEnum(Class<? extends Server> serverClass, Class<? extends Client> clientClass) {
        this.serverClass = serverClass;
        this.clientClass = clientClass;
    }

    public static NetEnum autoMatch(String name, NetEnum defaultEnum) {
        for (NetEnum item : NetEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultEnum;
    }
}