package com.lhk.example.provider;

import com.lhk.example.common.service.UserService;
import com.lhk.kkrpc.RpcApplication;
import com.lhk.kkrpc.config.RpcConfig;
import com.lhk.kkrpc.registry.LocalRegistry;
import com.lhk.kkrpc.server.HttpServer;
import com.lhk.kkrpc.server.VertxHttpServer;
import com.lhk.kkrpc.utils.ConfigUtils;

/**
 * 简易服务提供者示例（针对测试 kk-rpc-core）
 */
public class ProviderExample {

    public static void main(String[] args) {

        // 初始化 RPC 配置 (从 application.properties 文件中读取配置)
        RpcApplication.init();

        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
