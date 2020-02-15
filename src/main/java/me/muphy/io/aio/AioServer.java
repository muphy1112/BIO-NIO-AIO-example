package me.muphy.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AioServer {
    private final int port;

    public AioServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        new AioServer(8080).listen();
    }

    private void listen() throws IOException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);
        final AsynchronousServerSocketChannel socketChannel = AsynchronousServerSocketChannel.open();
        socketChannel.bind(new InetSocketAddress(this.port));
        System.out.println("服务器已启动，监听端口：" + this.port);
        socketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                System.out.println("IO操作成功，开始获取数据");
                try {
                    buffer.clear();
                    result.read(buffer).get();
                    buffer.flip();
                    result.write(buffer);
                    buffer.flip();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        result.close();
                        socketChannel.accept(null, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("操作完成");
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("IO操作失败：" + exc);
            }
        });

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
