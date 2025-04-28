package com.lhk.kkrpcspringbootstarter.bootstrap;

import com.lhk.kkrpc.RpcApplication;
import com.lhk.kkrpc.config.RegistryConfig;
import com.lhk.kkrpc.config.RpcConfig;
import com.lhk.kkrpc.model.ServiceMetaInfo;
import com.lhk.kkrpc.registry.LocalRegistry;
import com.lhk.kkrpc.registry.Registry;
import com.lhk.kkrpc.registry.RegistryFactory;
import com.lhk.kkrpcspringbootstarter.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Rpc 服务提供者启动
 */
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注册服务
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcServiceAnnotation = beanClass.getAnnotation(RpcService.class);
        // 如果有 @RpcService 注解，则注册服务
        if (rpcServiceAnnotation != null) {
            // 获取服务接口
            Class<?> interfaceClass = rpcServiceAnnotation.serviceInterface();
            // 默认值处理如果没有指定接口，则获取第一个接口
            if (interfaceClass == void.class) {
                interfaceClass = (Class<?>) beanClass.getInterfaces()[0];
            }
            // 获取服务名
            String serviceName = interfaceClass.getName();
            // 获取服务版本
            String serviceVersion = rpcServiceAnnotation.serviceVersion();

            // 注册服务
            // 本地注册
            LocalRegistry.register(serviceName, beanClass);
            // 获取 RPC 配置对象
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo =  new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 注册失败",e);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
