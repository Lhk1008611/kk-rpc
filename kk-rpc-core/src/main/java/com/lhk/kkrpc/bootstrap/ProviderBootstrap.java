package com.lhk.kkrpc.bootstrap;

import com.lhk.kkrpc.RpcApplication;
import com.lhk.kkrpc.config.RegistryConfig;
import com.lhk.kkrpc.config.RpcConfig;
import com.lhk.kkrpc.model.ServiceMetaInfo;
import com.lhk.kkrpc.registry.LocalRegistry;
import com.lhk.kkrpc.registry.Registry;
import com.lhk.kkrpc.registry.RegistryFactory;
import com.lhk.kkrpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * 服务提供者启动类
 */
public class ProviderBootstrap {
    public static void init(List<ServiceRegisterInfo> serviceRegisterInfos){

        // 初始化 RPC 配置 (从 application.properties 文件中读取配置)
        RpcApplication.init();
        // 获取 RPC 配置对象
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfos) {
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册服务
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 注册失败",e);
            }
        }

        // 启动 tcp 服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());

    }
}
