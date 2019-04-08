package com.zndbl.rpc.remoting.invoker.route;

import com.zndbl.rpc.remoting.invoker.route.impl.ZndblRpcLoadBalanceRoundStrategy;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public enum LoadBalance {

    ROUND(new ZndblRpcLoadBalanceRoundStrategy());

    public final ZndblRpcLoadBalance zndblRpcInvokerRouter;

    private LoadBalance(ZndblRpcLoadBalance zndblRpcInvokerRouter) {
        this.zndblRpcInvokerRouter = zndblRpcInvokerRouter;
    }

    public static LoadBalance match(String name, LoadBalance defaultRouter) {
        for (LoadBalance item : LoadBalance.values()) {
            if (item.equals(name)) {
                return item;
            }
        }
        return defaultRouter;
    }

}