package com.lhk.kkrpcspringbootstarter.annotation;

import com.lhk.kkrpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.lhk.kkrpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.lhk.kkrpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 rpc 注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 默认需要启动服务端
     * @return
     */
    boolean needServer() default true;
}
