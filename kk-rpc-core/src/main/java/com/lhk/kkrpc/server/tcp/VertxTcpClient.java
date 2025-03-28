package com.lhk.kkrpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.lhk.kkrpc.RpcApplication;
import com.lhk.kkrpc.model.RpcRequest;
import com.lhk.kkrpc.model.RpcResponse;
import com.lhk.kkrpc.model.ServiceMetaInfo;
import com.lhk.kkrpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VertxTcpClient {
    /**
     * rpc 框架用于发送 tcp 请求的客户端
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        // 发送 tcp 请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        // 由于 vertx 发送的是异步的 tcp 请求，所以需要使用 CompletableFuture 转异步为同步更方便获取请求结果
        CompletableFuture<RpcResponse> responseCompletableFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(),
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
                        // 接收响应消息，使用 TcpBufferHandlerWrapper 对响应的代码进行封装，解决响应的半包粘包问题
                        TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                            try {
                                ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                // 响应完成的时候将数据保存在 CompletableFuture 中
                                responseCompletableFuture.complete(rpcResponseProtocolMessage.getBody());
                            } catch (IOException e) {
                                throw new RuntimeException("接收 tcp 响应失败：消息解码错误/n" + e);
                            }
                        });
                        netSocket.handler(tcpBufferHandlerWrapper);
                    } else {
                        System.out.println("Connected to tcp server failed");
                    }
                });
        // 阻塞，直到响应完成，才会继续执行获取数据
        RpcResponse rpcResponse = responseCompletableFuture.get();
        // 关闭连接并返回响应数据
        netClient.close();
        return rpcResponse;
    }

}
