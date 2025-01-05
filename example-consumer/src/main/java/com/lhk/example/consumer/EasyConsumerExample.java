package com.lhk.example.consumer;

import com.lhk.example.common.model.User;
import com.lhk.example.common.service.UserService;
import com.lhk.kkrpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // 静态代理
//        UserService userService = new UserServiceProxy();
        // 动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("lhk");
        userService.getUser(user);
    }
}
