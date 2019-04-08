package com.zndbl.rpc.util;

import java.util.HashMap;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author LANWENJIAN
 * @Date 2019/4/8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （必须）
 */
public class ClassUtil {

    private static final HashMap<String, Class<?>> primClass = new HashMap<>();

    static {
        primClass.put("boolean", boolean.class);
        primClass.put("boolean", boolean.class);
        primClass.put("boolean", boolean.class);
        primClass.put("boolean", boolean.class);
        primClass.put("boolean", boolean.class);
        primClass.put("boolean", boolean.class);
        primClass.put("boolean", boolean.class);
    }

    public static Class<?> resolveClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            Class<?> cl = primClass.get(className);
            if (cl != null) {
                return cl;
            } else {
                throw ex;
            }
        }

    }
}