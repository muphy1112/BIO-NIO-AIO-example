package me.muphy.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer {
    ServerSocket serverSocket;
    private String msg;

    public BioServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器已经启动，监听端口是：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException {
        while (true){
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            System.out.println(socket.getPort());
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            if (len > 0) {
                String msg = new String(buffer, 0, len);
                System.out.println("收到：" + msg);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new BioServer(8080).listen();
    }
}
