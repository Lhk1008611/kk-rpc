package com.lhk.example.consumer;

import com.lhk.example.common.model.User;
import com.lhk.example.common.service.UserService;
import com.lhk.kkrpc.bootstrap.ConsumerBootstrap;
import com.lhk.kkrpc.proxy.ServiceProxyFactory;

/**
 * 服务消费者示例（针对测试 kk-rpc-core）
 */
public class ConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        ConsumerBootstrap.init();
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("lhk");
        // 远程调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
        userService.getUser(user);

        Thread.sleep(10000);

        userService.getUser(user);
    }
}
