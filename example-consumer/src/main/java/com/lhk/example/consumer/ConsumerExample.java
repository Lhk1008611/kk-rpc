package com.lhk.example.consumer;

import com.lhk.kkrpc.config.RpcConfig;
import com.lhk.kkrpc.utils.ConfigUtils;

/**
 * 简易服务消费者示例（针对测试 kk-rpc-core）
 */
public class ConsumerExample {

    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "kkrpc");
        System.out.println(rpc);
    }
}
