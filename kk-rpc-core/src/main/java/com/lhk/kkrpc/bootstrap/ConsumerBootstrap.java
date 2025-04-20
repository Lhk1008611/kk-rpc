package com.lhk.kkrpc.bootstrap;

import com.lhk.kkrpc.RpcApplication;

/**
 * 服务消费者启动类
 */
public class ConsumerBootstrap {

    /**
     * 初始化 RPC 配置
     */
    public static void init(){
        // 初始化 RPC 配置 (从 application.properties 文件中读取配置)
        RpcApplication.init();
    }
}
