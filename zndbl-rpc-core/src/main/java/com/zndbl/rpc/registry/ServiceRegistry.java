package com.zndbl.rpc.registry;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/19
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public interface ServiceRegistry {

    /**
     * 注册服务信息
     * @param applicationName 应用名称
     * @param serviceName 服务名称
     * @param serviceAddress 服务地址
     */
    void registry(String applicationName, String serviceName, String serviceAddress);
}