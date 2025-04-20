package com.lhk.example.provider;

import com.lhk.example.common.service.UserService;
import com.lhk.kkrpc.bootstrap.ProviderBootstrap;
import com.lhk.kkrpc.bootstrap.ServiceRegisterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务提供者示例（针对测试 kk-rpc-core）
 */
public class ProviderExample {

    public static void main(String[] args) {
        // 要注册的服务列表
        List<ServiceRegisterInfo> serviceRegisterInfos = new ArrayList<>();
        ServiceRegisterInfo userServiceRegisterInfo = new ServiceRegisterInfo(UserService.class.getName(),UserServiceImpl.class);
        serviceRegisterInfos.add(userServiceRegisterInfo);
        // 服务提供者初始化并启动服务器
        ProviderBootstrap.init(serviceRegisterInfos);
    }
}

