package me.muphy.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    //准备两个东西  轮询器（叫号系统）+缓冲区（等候区）
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private int port = 8080;

    public NioServer(int port) {
        try {
            this.port = port;
            ServerSocketChannel socketChannel = ServerSocketChannel.open();
            socketChannel.bind(new InetSocketAddress(this.port));
            //为了兼容BIO NIO默认使用阻塞
            socketChannel.configureBlocking(false);

            //叫号系统工作
            selector = Selector.open();

            //开门营业
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        System.out.println("listen on " + this.port + ".");
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    process(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel channel = serverSocketChannel.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            int len = channel.read(buffer);
            if (len > 0) {
                buffer.flip();
                String content = new String(buffer.array(), 0, len);
                channel.register(selector, SelectionKey.OP_WRITE);
                //在KEY上携带一个附件，一会儿写出去
                key.attach(content);
                System.out.println("读取内容：" + content);
            }
        } else if (key.isWritable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            String attachment = (String) key.attachment();
            channel.write(ByteBuffer.wrap(("输出：" + attachment).getBytes()));
            channel.close();
        }
    }

    public static void main(String[] args) {
        new NioServer(8080).listen();
    }
}
