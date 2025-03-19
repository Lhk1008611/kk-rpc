# kk-rpc
kk-rpc 框架

# etcd

```bash
# 开启服务器端口
firewall-cmd --zone=public --add-port=8889/tcp --permanent
# 重新加载防火墙
firewall-cmd --reload
netstat -ntulp |grep 8889

# 启动 etcd
/tmp/etcd-download-test/etcd --listen-client-urls http://0.0.0.0:2379 --advertise-client-urls http://0.0.0.0:2379 --listen-peer-urls http://0.0.0.0:2380

# 启动 etcdkeeper
/opt/etcdkeeper-v0.7.8/etcdkeeper -p 8889

# 后台启动
nohup /tmp/etcd-download-test/etcd --listen-client-urls http://0.0.0.0:2379 --advertise-client-urls http://0.0.0.0:2379 --listen-peer-urls http://0.0.0.0:2380 > /tmp/etcd.log 2>&1 &

nohup /opt/etcdkeeper-v0.7.8/etcdkeeper -p 8889 /opt/etcdkeeper-v0.7.8/etcdkeeper.log 2>&1 &

/tmp/etcd-download-test/etcdctl --endpoints=localhost:2379 put foo bar
```



# 自定义 rpc 传输协议

## 需求分析

- 前面实现自定义 rpc 框架是使用 Vert.x 的 HttpServer 作为服务提供者的服务器，代码实现比较简单，其底层网络传输使用的是 HTTP 协议
  - 但是HTTP 只是 RPC 框架网络传输的一种可选方式罢了
  - 一般情况下，RPC 框架会比较注重性能，而 HTTP 协议中的头部信息、请求响应格式较“重”，会影响网络传输性能
  - 所以，我们需要自己自定义一套 RPC 协议，比如利用 TCP 等传输层协议、自己定义请求响应结构，来实现性能更高、
    更灵活、更安全的 RPC 框架

## 设计方案

### RPC 协议核心部分

1. 自定义网络传输
2. 自定义消息结构

### 网络传输设计（tcp 协议）

- 网络传输设计的目标是: 选择一个能够高性能通信的**网络协议**和**传输方式**
- 需求分析中已经提到了，HTTP 协议的头信息是比较大的，会影响传输性能。但其实除了这点外，HTTP 本身属于无状态协议，这意味着每个 HTTP 请求都是独立的，每次请求/响应都要重新建立和关闭连接，也会影响性能
  - 考虑到这点，在 HTTP/1.1 中引入了持久连接(**Keep-Alive**)，允许在单个TCP 连接上发送多个 HTTP 请求和响应，避免了每次请求都要重新建立和关闭连接的开销
  - 虽然如此，HTTP 本身是应用层协议，我们现在设计的 RPC 协议也是应用层协议，性能肯定是不如底层(传输层)的 TCP协议要高的。所以我们想要追求更高的性能，还是选择使用 **TCP 协议**完成网络传输，有更多的自主设计空间

### 消息结构设计

- 消息结构设计的目标是:用 **最少的** 空间传递 **需要的信息**

- 如何使用最少的空间呢?

  - 之前接触到的数据类型可能都是整型、长整型、浮点数类型等等这些类型其实都比较“重”，占用的字节数较多。
    - 比如整型要占用 `4 个字节 = 32 个 bit 位`
  - 我们在自定义消息结构时，想要节省空间，就要尽可能使用更轻量的类型，比如 byte 字节类型，只占用 `1个字节 = 8个bit 位`
    - 需要注意的是，java 中实现 bit 位运算拼接相对比较麻烦，所以权衡开发成本，我们设计消息结构时，尽量给每个数据凑到整个字节

- 消息内需要哪些信息呢?

  - 目标肯定是能够完成请求嘛，那可以参考 HTTP 请求方式，找到一些线索

  - 分析 HTTP 请求结构，我们能够得到 RPC 消息所需的信息:

    - **魔数**: 作用是安全校验，防止服务器处理了非框架发来的乱七八糟的消息(类似 HTTPS 的安全证书)
    - **版本号**: 保证请求和响应的一致性(类似 HTTP 协议有 1.0/2.0 等版本)
    - **序列化方式**: 来告诉服务端和客户端如何解析数据(类似 HTTP 的 Content-Type 内容类型)
    - **类型**: 标识是请求还是响应?或者是心跳检测等其他用途。(类似 HTTP 有请求头和响应头)
    - **状态**: 如果是响应，记录响应的结果(类似 HTTP 的 200 状态代码)
    - 此外，还需要有**请求 id**，唯一标识某个请求，因为 TCP 是双向通信的，需要有个唯一标识来追踪每个请求
    - 最后最重要的是，要发送 **body 内容数据**。我们暂时称它为 **请求体**，类似于之前 HTTP 请求中发送的 RpcRequest
      - 如果是 HTTP 这种协议，有专门的 `key/value` 结构，很容易找到完整的 body 数据。但基于 TCP 协议，想要获取到完整的 body 内容数据，就需要一些“小心思”了，因为 TCP 协议本身会存在**半包和粘包问题**，每次传输的数据可能是不完整的
      - 所以我们需要在消息头中新增一个字段 **请求体数据长度** ，保证能够完整地获取 body 内容信息

  - 所以消息结构设计如下图

    ![image-20250221221335115](README.assets/image-20250221221335115.png)

  - 实际上，这些数据应该是紧凑的，请求头信息总长 17 个字节。也就是说，上述消息结构，本质上就是拼接在一起的字节数组。后续实现时，需要有 **消息编码器** 和 **消息解码器**

    - 编码器先 new 一个空的 Bufer 缓冲区，然后按照顺向缓冲区依次写入这些数据
    - 解码器在读取时也按照顺序依次读取，就能还原出编码前的数据

  - 通过这种约定的方式，我们就不用记录头信息了。比如 magic 魔数，不用存储“magic” 这个字符串，而是读取第一个字节(前 8 bit)就能获取到。Redis 底层很多数据结构也都是这种设计

  - 例如参考 doubbo 的协议设计

    ![/dev-guide/images/dubbo_protocol_header.jpg](README.assets/dubbo_protocol_header.png)

### 开发实现

#### 1、消息结构实现

1. 创建协议消息结构类

   ```java
   package com.lhk.kkrpc.protocol;
   
   import lombok.AllArgsConstructor;
   import lombok.Data;
   import lombok.NoArgsConstructor;
   
   /**
    * 协议消息结构
    */
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class ProtocolMessage<T> {
   
       /**
        * 消息头
        */
       private Header header;
   
       /**
        * 消息体（请求或响应对象）
        */
       private T body;
   
       /**
        * 协议消息头
        */
       @Data
       public static class Header {
   
           /**
            * 魔数，保证安全性
            */
           private byte magic;
   
           /**
            * 版本号
            */
           private byte version;
   
           /**
            * 序列化器
            */
           private byte serializer;
   
           /**
            * 消息类型（请求 / 响应）
            */
           private byte type;
   
           /**
            * 状态
            */
           private byte status;
   
           /**
            * 请求 id
            */
           private long requestId;
   
           /**
            * 消息体长度
            */
           private int bodyLength;
       }
   
   }
   ```

2. 新建协议常量类，记录和自定义协议有关的关键信息，比如消息头长度、魔数、版本号

   ```java
   package com.lhk.kkrpc.protocol;
   
   /**
    * 协议常量
    */
   public interface ProtocolConstant {
   
       /**
        * 消息头长度
        */
       int MESSAGE_HEADER_LENGTH = 17;
   
       /**
        * 协议魔数（16进制）
        */
       byte PROTOCOL_MAGIC = 0x1;
   
       /**
        * 协议版本号（16进制）
        */
       byte PROTOCOL_VERSION = 0x1;
   }
   ```

3. 新建消息字段的枚举类

   - **协议状态枚举**，暂时只定义**成功**、**请求失败**、**响应失败**三种枚举值:

     ```java
     package com.lhk.kkrpc.protocol;
     
     import lombok.Getter;
     
     /**
      * 协议消息的状态枚举
      */
     @Getter
     public enum ProtocolMessageStatusEnum {
     
         OK("ok", 20),
         BAD_REQUEST("badRequest", 40),
         BAD_RESPONSE("badResponse", 50);
     
         private final String text;
     
         private final int value;
     
         ProtocolMessageStatusEnum(String text, int value) {
             this.text = text;
             this.value = value;
         }
     
         /**
          * 根据 value 获取枚举
          *
          * @param value
          * @return
          */
         public static ProtocolMessageStatusEnum getEnumByValue(int value) {
             for (ProtocolMessageStatusEnum anEnum : ProtocolMessageStatusEnum.values()) {
                 if (anEnum.value == value) {
                     return anEnum;
                 }
             }
             return null;
         }
     }
     ```

   - 协议消息类型枚举，包括请求、响应、心跳、其他

     ```java
     package com.lhk.kkrpc.protocol;
     
     import lombok.Getter;
     
     /**
      * 协议消息的类型枚举
      */
     @Getter
     public enum ProtocolMessageTypeEnum {
     
         REQUEST(0),
         RESPONSE(1),
         HEART_BEAT(2),
         OTHERS(3);
     
         private final int key;
     
         ProtocolMessageTypeEnum(int key) {
             this.key = key;
         }
     
         /**
          * 根据 key 获取枚举
          *
          * @param key
          * @return
          */
         public static ProtocolMessageTypeEnum getEnumByKey(int key) {
             for (ProtocolMessageTypeEnum anEnum : ProtocolMessageTypeEnum.values()) {
                 if (anEnum.key == key) {
                     return anEnum;
                 }
             }
             return null;
         }
     }
     ```

   - 协议消息的序列化器枚举，与自定义 RPC 框架已支持的序列化器对应

     ```java
     package com.lhk.kkrpc.protocol;
     
     import cn.hutool.core.util.ObjectUtil;
     import lombok.Getter;
     
     import java.util.Arrays;
     import java.util.List;
     import java.util.stream.Collectors;
     
     /**
      * 协议消息的序列化器枚举
      */
     @Getter
     public enum ProtocolMessageSerializerEnum {
     
         JDK(0, "jdk"),
         JSON(1, "json"),
         KRYO(2, "kryo"),
         HESSIAN(3, "hessian");
     
         private final int key;
     
         private final String value;
     
         ProtocolMessageSerializerEnum(int key, String value) {
             this.key = key;
             this.value = value;
         }
     
         /**
          * 获取值列表
          *
          * @return
          */
         public static List<String> getValues() {
             return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
         }
     
         /**
          * 根据 key 获取枚举
          *
          * @param key
          * @return
          */
         public static ProtocolMessageSerializerEnum getEnumByKey(int key) {
             for (ProtocolMessageSerializerEnum anEnum : ProtocolMessageSerializerEnum.values()) {
                 if (anEnum.key == key) {
                     return anEnum;
                 }
             }
             return null;
         }
     
     
         /**
          * 根据 value 获取枚举
          *
          * @param value
          * @return
          */
         public static ProtocolMessageSerializerEnum getEnumByValue(String value) {
             if (ObjectUtil.isEmpty(value)) {
                 return null;
             }
             for (ProtocolMessageSerializerEnum anEnum : ProtocolMessageSerializerEnum.values()) {
                 if (anEnum.value.equals(value)) {
                     return anEnum;
                 }
             }
             return null;
         }
     }
     ```



#### 2、网络传输实现（基于 Vert.x 的 TCP 传输）

前面 RPC 框架使用了高性能的 Vert.x 作为网络传输服务器，之前用的是 **HttpServer**。同样，Vert.x 也支持 TCP 服务器，相比于 Netty 或者自己写 Socket 代码，更加简单易用

1. TCP 服务器实现

   新建 VertxTcpServer 类，跟之前写的 VertxHttpServer 类似，先创建 Vert.x 的服务器实例，然后定义处理请求的方
   法，比如回复“Hello, client!”，最后启动服务器

   ```
   
   ```

   

2. 

