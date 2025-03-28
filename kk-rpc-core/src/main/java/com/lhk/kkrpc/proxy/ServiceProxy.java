package com.lhk.kkrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import com.lhk.kkrpc.RpcApplication;
import com.lhk.kkrpc.config.RpcConfig;
import com.lhk.kkrpc.constant.RpcConstant;
import com.lhk.kkrpc.model.RpcRequest;
import com.lhk.kkrpc.model.RpcResponse;
import com.lhk.kkrpc.model.ServiceMetaInfo;
import com.lhk.kkrpc.registry.Registry;
import com.lhk.kkrpc.registry.RegistryFactory;
import com.lhk.kkrpc.server.tcp.VertxTcpClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务代理（JDK 动态代理）（客户端发送请求时对请求进行处理后再发送）
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理发送请求
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            // todo 后续做负载均衡
            // 暂时先取第一个
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
            // 发送 tcp 请求
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            return rpcResponse.getData();
        }catch (Exception e){
            throw new RuntimeException("rpc 服务调用失败");
        }
    }
}
