package com.zndbl.rpc.net.netty;

import com.zndbl.rpc.net.common.ZndblRpcRequest;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/22
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public interface Client {

    void asyncSend(String address, ZndblRpcRequest zndblRpcRequest);
}