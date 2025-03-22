package com.lhk.kkrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.lhk.kkrpc.RpcApplication;
import com.lhk.kkrpc.config.RpcConfig;
import com.lhk.kkrpc.constant.RpcConstant;
import com.lhk.kkrpc.model.RpcRequest;
import com.lhk.kkrpc.model.RpcResponse;
import com.lhk.kkrpc.model.ServiceMetaInfo;
import com.lhk.kkrpc.protocol.*;
import com.lhk.kkrpc.registry.Registry;
import com.lhk.kkrpc.registry.RegistryFactory;
import com.lhk.kkrpc.serializer.Serializer;
import com.lhk.kkrpc.serializer.SerializerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 服务代理（JDK 动态代理）
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
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
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        // 由于 vertx 发送的是异步的 tcp 请求，所以需要使用 CompletableFuture 转异步为同步更方便获取请求结果
        CompletableFuture<RpcResponse> responseCompletableFuture = new CompletableFuture<>();
        netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceHost(),
                (connectResult) -> {
                    // 判断是否连接成功
                    if (connectResult.succeeded()) {
                        System.out.println("Connected to tcp server succeeded");
                        // 获取 netSocket 连接,用于发送数据
                        NetSocket netSocket = connectResult.result();
                        // 构造 tcp 请求消息
                        ProtocolMessage<RpcRequest> rpcRequesttProtocolMessage = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                        // 指定序列化器
                        header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                        header.setRequestId(IdUtil.getSnowflakeNextId());
                        rpcRequesttProtocolMessage.setHeader(header);
                        rpcRequesttProtocolMessage.setBody(rpcRequest);
                        //编码请求消息
                        try {
                            Buffer encodeBuffer = ProtocolMessageEncoder.encode(rpcRequesttProtocolMessage);
                            netSocket.write(encodeBuffer);
                        } catch (IOException e) {
                            throw new RuntimeException("tcp 请求发送失败：消息编码错误/n" + e);
                        }
                        // 接收响应消息
                        netSocket.handler(buffer -> {
                            try {
                                ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                // 响应完成的时候将数据保存在 CompletableFuture 中
                                responseCompletableFuture.complete(rpcResponseProtocolMessage.getBody());
                            } catch (IOException e) {
                                throw new RuntimeException("接收 tcp 响应失败：消息解码错误/n" + e);
                            }
                        });
                }else {
                        System.out.println("Connected to tcp server failed");
                    }
            });
        // 阻塞，直到响应完成，才会继续执行获取数据
        RpcResponse rpcResponse = responseCompletableFuture.get();
        // 关闭连接并返回响应数据
        netClient.close();
        return rpcResponse.getData();
    }
}
