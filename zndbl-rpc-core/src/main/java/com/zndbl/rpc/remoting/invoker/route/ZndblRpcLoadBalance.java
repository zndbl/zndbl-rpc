package com.zndbl.rpc.remoting.invoker.route;

import java.util.TreeSet;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public abstract class ZndblRpcLoadBalance {

    public abstract String route(String serviceKey, TreeSet<String> addressSet);
}