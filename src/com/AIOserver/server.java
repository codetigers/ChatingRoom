package com.AIOserver;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: ChatingRoom
 * @description:
 * @author: Mr.li
 * @create: 2020-07-14 20:45
 **/
public class server {
    final String LOCALHOST = "localhost";
    final int DEFAULTPORT = 8888;
    AsynchronousServerSocketChannel serverChannel;

    private void close(Closeable closeable){
        if(closeable!=null){
            try {
                System.out.println("关闭"+closeable);
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        //绑定监听端口
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(LOCALHOST,DEFAULTPORT));
            System.out.println("启动服务，监听端口:"+DEFAULTPORT);
            while(true){
                serverChannel.accept(null,new AcceptHandler());
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(serverChannel);
        }
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            if(serverChannel.isOpen()){
                serverChannel.accept(null,this);
            }
            AsynchronousSocketChannel clientChannel = result;
            if(clientChannel!=null&&clientChannel.isOpen()){
                ClientHandler handler = new ClientHandler(clientChannel);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Map<String,Object> info = new HashMap<>();
                info.put("type","read");
                info.put("buffer",buffer);

                clientChannel.read(buffer,info,handler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }

    private class ClientHandler implements CompletionHandler<Integer,Object>{
        private AsynchronousSocketChannel clientcChannel;

        public ClientHandler(AsynchronousSocketChannel channel){
            this.clientcChannel = channel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            Map<String,Object> info = (Map<String, Object>) attachment;
            String type = (String) info.get("type");
            if("read".equals(type)){
                ByteBuffer buffer = (ByteBuffer) info.get("buffer");
                buffer.flip();
                info.put("type","write");
                clientcChannel.write(buffer,info,this);
                buffer.clear();
            }else if("write".equals(type)){
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                info.put("type","read");
                info.put("buffer",buffer);

                clientcChannel.read(buffer,info,this);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            //处理错误
        }
    }

    public static void main(String[] args) {
        server server = new server();
        server.start();
    }

}


