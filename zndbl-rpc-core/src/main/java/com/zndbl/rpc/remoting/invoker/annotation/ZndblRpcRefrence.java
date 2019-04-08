package com.zndbl.rpc.remoting.invoker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zndbl.rpc.remoting.invoker.call.CallType;
import com.zndbl.rpc.remoting.invoker.route.LoadBalance;
import com.zndbl.rpc.remoting.net.NetEnum;
import com.zndbl.rpc.serialize.Serializer;

/**
 *
 * @author LANWENJIAN
 * @Date 2019/4/3
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ZndblRpcRefrence {

    NetEnum netType() default NetEnum.NETTY;
    Serializer.SerializeEnum serializer() default Serializer.SerializeEnum.HESSIAN;
    CallType callType() default CallType.SYNC;
    LoadBalance loadBalance() default LoadBalance.ROUND;

    String version() default "";

    long timeout() default 1000;

    String address() default "";
    String accessToken() default "";


}