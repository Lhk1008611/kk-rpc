package com.lhk.example.provider;

import com.lhk.example.common.service.UserService;
import com.lhk.kkrpc.registry.LocalRegistry;
import com.lhk.kkrpc.server.HttpServer;
import com.lhk.kkrpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {

    public static void main(String[] args) {

        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8888);
    }
}
