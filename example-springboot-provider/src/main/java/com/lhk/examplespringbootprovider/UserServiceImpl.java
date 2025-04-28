package com.lhk.examplespringbootprovider;

import com.lhk.example.common.model.User;
import com.lhk.example.common.service.UserService;
import com.lhk.kkrpcspringbootstarter.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * 示例服务实现类
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("远程调用前用户名：" + user.getName());
        user.setName("lhk1008611");
        return user;
    }
}
