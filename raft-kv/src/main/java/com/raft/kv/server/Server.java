package com.raft.kv.server;

import com.raft.core.node.Node;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// *@author liuyaolong
public class Server {

    private static final Logger logger= LoggerFactory.getLogger(Server.class);
    private final Node node;
    private final int port;
    private final Service service;
    private final NioEventLoopGroup bossGroup=new NioEventLoopGroup(1);
    private final NioEventLoopGroup workerGroup=new NioEventLoopGroup(4);

    //构造函数，Node实例和服务端口
    public Server(Node node, int port) {
        this.node = node;
        this.port = port;
        this.service=new Service(node);
    }
    //启动
    public void start() throws Exception {
        this.node.start();

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new Encoder());//序列化
                        pipeline.addLast(new Decoder());//反序列化
                        pipeline.addLast(new ServiceHandler(service));//实际服务处理
                    }
                });
        logger.info("server started at port {}", this.port);
        serverBootstrap.bind(this.port);//在指定端口启动
    }
    //关闭
    public void stop() throws Exception {
        logger.info("stopping server");
        this.node.stop();
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }
}
