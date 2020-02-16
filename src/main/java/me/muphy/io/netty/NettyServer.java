package me.muphy.io.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyServer {

    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyServer(8080).start();
    }

    private void start() throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            //            这里面写逻辑
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
//                new HttpResponseDecoder() 自定义编解码器
//                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4));
//                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast("encoder", new ObjectEncoder());
                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                pipeline.addLast(new ServerChannelHandler());
            }
        }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        //正式启动服务器
        ChannelFuture future = bootstrap.bind(this.port).sync();
        System.out.println("服务器启动，监听端口：" + this.port);
        future.channel().closeFuture().sync();
    }

    //业务处理
    private class ServerChannelHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String res = "服务端收到消息：" + msg;
            System.out.println(res);
            ctx.write("服务端回复消息：" + msg);
            ctx.flush();
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("与客户端交互发生异常：" + ctx.name());
        }
    }
}
