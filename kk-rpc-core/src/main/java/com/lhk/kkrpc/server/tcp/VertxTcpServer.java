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

            String correctMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
            int correctMessageLength = correctMessage.getBytes().length;

            // 构造 RecordParser, 为 Parser 指定每次读取固定值长度的内容
            RecordParser recordParser = RecordParser.newFixed(correctMessageLength);
            recordParser.setOutput(new Handler<Buffer>() {
                @Override
                public void handle(Buffer buffer) {
                    // 处理接收到的字节数组
                    byte[] requestData = buffer.getBytes();
                    String requestString = new String(requestData);
                    System.out.println("correct message length: " + correctMessageLength);
                    System.out.println("Received message length: " + requestData.length);
                    System.out.println("Received message: " + requestString);
                    if (requestString.equals(correctMessage)){
                        System.out.println("Received correct message");
                    }else {
                        System.out.println("Received incorrect message");
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
