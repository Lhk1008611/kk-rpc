package com.lhk.examplespringbootconsumer;

import com.lhk.example.common.model.User;
import com.lhk.example.common.service.UserService;
import com.lhk.kkrpcspringbootstarter.annotation.RpcReference;
import org.springframework.stereotype.Service;

/**
 * 远程调用示例
 */
@Service
public class ExampleConsumer {

    @RpcReference
    private static UserService userService;

    public  void consumer() {
        User user = new User();
        user.setName("lhk");
        System.out.println("远程调用后用户名："+userService.getUser(user));
    }
}
