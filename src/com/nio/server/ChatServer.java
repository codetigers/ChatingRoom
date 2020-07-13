package com.nio.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @program: ChatingRoom
 * @description: 因为使用NIO的时候是非阻塞式的IO，因此可以使用一个线程来控制聊天室信息的传递，所以不需要建多个线程和使用线程池
 * @author: Mr.li
 * @create: 2020-07-08 15:09
 **/
public class ChatServer {
    private static final int DEFAUT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);//读取客户端发送来的数据，添加到rBuffer中
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);//将服务器要给其它客户端的数据保存起来
    private Charset charset = Charset.forName("UTF-8");//确定编码解码模式
    private int port;//允许用户创建按自定义端口


    public ChatServer(){
        this(DEFAUT_PORT);
    }

    public ChatServer(int port){
        this.port = port;
    }

    public void start(){
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);//保证这个通道是非阻塞式的
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口"+port+"...");

            while(true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key:selectionKeys
                     ) {
                    handles(key); //处理key,来接收或处理
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(selector);
        }
    }

    public void close(Closeable close){
        if(close!=null){
            try {
                close.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handles(SelectionKey key) throws IOException {
        //accept--和客户端建立了连接
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            try {
                SocketChannel client = server.accept();
                client.configureBlocking(false);
                client.register(selector,SelectionKey.OP_READ);
                System.out.println(getClientName(client)+"连接");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            String fwdMsg = receive(client);
            if(fwdMsg.isEmpty()){
                key.cancel();
                selector.wakeup();
                System.out.println(getClientName(client)+"断开");
            }else{
                forwardMessage(client,fwdMsg);
                if(readyToQuit(fwdMsg)){
                    key.cancel();
                    selector.wakeup();
                    System.out.println(getClientName(client)+"断开");
                }
            }
        }
        //read--客户端发送了消息
    }

    private String getClientName(SocketChannel client){
        return "客户端["+client.socket().getPort()+"]:";
    }

    private boolean readyToQuit(String fwdMsg) {
        return fwdMsg.equals("quit");
    }

    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        for (SelectionKey key: selector.keys()
             ) {
            Channel channel = key.channel();
            if(channel instanceof ServerSocketChannel){
                continue;
            }
            if(key.isValid()&&!client.equals(channel)){
                wBuffer.clear();
                wBuffer.put(charset.encode(getClientName((SocketChannel) channel)+fwdMsg));
                wBuffer.flip();
                while(wBuffer.hasRemaining()){
                    ((SocketChannel) channel).write(wBuffer);
                }
            }
        }
    }


    private String receive(SocketChannel client) throws IOException {

            rBuffer.clear();
            while(client.read(rBuffer)>0);
            rBuffer.flip();
            return String.valueOf(charset.decode(rBuffer));

    }


    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(7777);
        chatServer.start();
    }
}
