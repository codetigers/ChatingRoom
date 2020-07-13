package com.nio.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @program: ChatingRoom
 * @description: 客户端(发送消息，连接，接收消息，关闭资源，判断退出，开启)
 * @author: Mr.li
 * @create: 2020-07-08 15:15
 **/
public class ChatClient {
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;
    private String host;
    private int port;


    private SocketChannel client;
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);//读取客户端发送来的数据，添加到rBuffer中
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);//将服务器要给其它客户端的数据保存起来
    private Charset charset = Charset.forName("UTF-8");//确定编码解码模式

    public ChatClient(){
        this(DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT);
    }

    public ChatClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    public void start()  {
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);
            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(host,port));

            while(true){
                selector.select();
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                for (SelectionKey key:selectionKeySet
                ) {
                    handles(key);
                }
                selectionKeySet.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ClosedSelectorException e){       //将用户再使用quit退出连接所抛出的异常捕获

        }finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        //connect事件，就绪事件
        if(key.isConnectable()){
            SocketChannel client = (SocketChannel) key.channel();
            if(client.isConnectionPending()){
                client.finishConnect();
                new Thread(new UserInputHandler(this)).start();
            }
            client.register(selector,SelectionKey.OP_READ);
        }
        //read，服务器转发消息
        else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            String msg = receive(client);
            if(msg.isEmpty()){
                close(selector);
            }
            System.out.println(msg);
        }
    }
    //发送消息

    public void send(String msg) throws IOException {
        if(msg.isEmpty()){
            return;
        }
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while(wBuffer.hasRemaining()){
            client.write(wBuffer);
        }

        //检查用户是否退出
        if(readyToQuit(msg)){
            close(selector);
        }
    }

    public String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer)>0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    public boolean readyToQuit(String msg){
        return msg.equals(QUIT);
    }

    public void close(Closeable closeable){
        if(closeable!=null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient("127.0.0.1",7777);
        chatClient.start();
    }
}
