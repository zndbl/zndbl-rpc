package com.zndbl.rpc.util;

import java.util.HashMap;
import java.util.Map;

import com.zndbl.rpc.net.common.ZndblRpcResponse;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/23
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class MapUtil {

    private static final Map<String, ZndblRpcResponse> holder = new HashMap<>();

    public static void putResponse(String requestId, ZndblRpcResponse zndblRpcResponse) {
        holder.put(requestId, zndblRpcResponse);
    }

    public static ZndblRpcResponse getResponse(String requestId) {
        return holder.get(requestId);
    }

    public static void clearResponse() {
        holder.clear();
    }
}