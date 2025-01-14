package com.lhk.example.provider;

import com.lhk.example.common.service.UserService;
import com.lhk.kkrpc.RpcApplication;
import com.lhk.kkrpc.config.RegistryConfig;
import com.lhk.kkrpc.config.RpcConfig;
import com.lhk.kkrpc.model.ServiceMetaInfo;
import com.lhk.kkrpc.registry.LocalRegistry;
import com.lhk.kkrpc.registry.Registry;
import com.lhk.kkrpc.registry.RegistryFactory;
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
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}

