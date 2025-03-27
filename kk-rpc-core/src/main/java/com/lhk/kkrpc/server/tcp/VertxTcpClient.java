package com.lhk.kkrpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class VertxTcpClient {
    /**
     * 示例客户端
     */
    public void start() {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("Connected to TCP server");
                io.vertx.core.net.NetSocket socket = result.result();
                // 发送数据，测试半包粘包(不固定长度的消息体)
                for (int i = 0; i < 10000; i++) {
                    String str = "Hello, server!Hello, server!Hello, server!Hello, server!"+ i;
                    // 发送数据
                    Buffer buffer = Buffer.buffer();
                    //模拟 header，8个字节
                    buffer.appendInt(0);  // 4个字节
                    buffer.appendInt(str.getBytes().length); // 4个字节
                    buffer.appendBytes(str.getBytes());
                    socket.write(buffer);
                }
                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("Received response from server: " + buffer.toString());
                });
            } else {
                System.err.println("Failed to connect to TCP server");
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
