package com.lhk.kkrpc.server.tcp;

import com.lhk.kkrpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

public class VertxTcpServer implements HttpServer {

    /**
     * 示例请求处理逻辑
     * @param requestData
     * @return
     */
    private byte[] handleRequest(byte[] requestData) {
        // 在这里编写处理请求的逻辑，根据 requestData 构造响应数据并返回
        // 这里只是一个示例，实际逻辑需要根据具体的业务需求来实现
        // 演示半包粘包
        String correctMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
        int correctMessageLength = correctMessage.getBytes().length;
        System.out.println("正确的接收的数据，length:" + correctMessageLength);
        if (correctMessageLength < requestData.length){
            System.out.println("粘包，length = " + requestData.length);
        }
        if (correctMessageLength > requestData.length){
            System.out.println("半包，length = " + requestData.length);
        }
        if (correctMessageLength == requestData.length){
            System.out.println("Received request: " + new String(requestData));
        }
        return "Hello, client!".getBytes();
    }

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
            // 处理连接
            socket.handler(buffer -> {
                // 处理接收到的字节数组
                byte[] requestData = buffer.getBytes();
                // 在这里进行自定义的字节数组处理逻辑，比如解析请求、调用服务、构造响应等
                byte[] responseData = handleRequest(requestData);
                // 发送响应
//                socket.write(Buffer.buffer(responseData));
            });
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
