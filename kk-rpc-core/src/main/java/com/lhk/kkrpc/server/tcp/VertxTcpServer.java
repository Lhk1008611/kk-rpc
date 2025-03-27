package com.lhk.kkrpc.server.tcp;

import com.lhk.kkrpc.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;

public class VertxTcpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        // 创建 TCP 服务器
        NetServer server = vertx.createNetServer();

        // 请求处理
//        server.connectHandler(new TcpServerHandler());

        // 示例处理请求
        server.connectHandler(socket -> {

            // 构造 RecordParser, 为 Parser 指定每次读取固定值长度的内容，8个字节是读取 header 的信息
            RecordParser recordParser = RecordParser.newFixed(8);
            recordParser.setOutput(new Handler<Buffer>() {
                // 初始化消息体的容量
                int bodySize = -1;
                // 用于接收一次完整的读取（头 + 体）
                Buffer resultBuffer = Buffer.buffer();
                @Override
                public void handle(Buffer buffer) {
                    if (bodySize == -1){
                        // 读取消息体的长度
                        bodySize = buffer.getInt(4);
                        // 读取消息体的内容
                        recordParser.fixedSizeMode(bodySize);
                        // 将消息头添加到 resultBuffer 中
                        resultBuffer.appendBuffer(buffer);
                    }else{
                        // 将消息体添加到 resultBuffer 中
                        resultBuffer.appendBuffer(buffer);
                        System.out.println("resultBuffer:" + new String(buffer.getBytes()));
                        // 重置一轮，为下次读取消息头做准备
                        recordParser.fixedSizeMode(8);
                        bodySize = -1;
                        resultBuffer = Buffer.buffer();
                    }
                }
            });

            // 处理连接
            socket.handler(recordParser);
        });

        // 启动 TCP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP server started on port " + port);
            } else {
                System.err.println("Failed to start TCP server: " + result.cause());
            }
        });
    }


    // 测试运行 tcp 服务器
    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
