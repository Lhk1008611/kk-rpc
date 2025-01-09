package com.lhk.example.consumer;

import com.lhk.example.common.mockService.UserService;
import com.lhk.example.common.model.User;
import com.lhk.kkrpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例（针对测试 kk-rpc-core）
 */
public class ConsumerExample {

    public static void main(String[] args) {
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("yupi");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
        long number = userService.getNumber();
        System.out.println(number);
    }
}
