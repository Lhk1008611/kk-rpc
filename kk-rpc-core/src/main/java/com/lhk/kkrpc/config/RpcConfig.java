package com.lhk.kkrpc.config;

import com.lhk.kkrpc.fault.retry.RetryStrategyKeys;
import com.lhk.kkrpc.fault.tolerant.TolerantStrategyKeys;
import com.lhk.kkrpc.loadbalancer.LoadBalancerKeys;
import com.lhk.kkrpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "kk-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";
    
    /**
     * 服务器端口号
     */
    private Integer serverPort = 8888;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;
}

