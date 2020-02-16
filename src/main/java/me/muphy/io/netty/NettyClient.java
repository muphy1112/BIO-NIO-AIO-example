package me.muphy.io.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        new NettyClient().connect();
    }

    public void connect() throws InterruptedException {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
//                new HttpResponseDecoder() 自定义编解码器
//                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4));
//                        pipeline.addLast(new LengthFieldPrepender(4));
                        pipeline.addLast("encoder", new ObjectEncoder());
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                        pipeline.addLast(new ClientChannelHandler());
                    }
                });
        //正式链接
        ChannelFuture future = bootstrap.connect("localhost", 8080).sync();
        future.channel().writeAndFlush("测试数据").sync();
        future.channel().closeFuture().sync();

    }

    private class ClientChannelHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("连接服务器发生异常");
        }
    }

}
