package com.li.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: ChatingRoom
 * @description: 服务器(连接，使用Map存储Socket写，将Socket写添加到Map,删除，打印，关闭，判断退出，开启)
 * 使用了线程池来保存创建的线程能使其重用，降低了资源消耗
 * @author: Mr.li
 * @create: 2020-07-08 15:09
 **/
public class ChatServer {
    private int DEFAUT_PORT = 8888;
    private final String QUIT = "quit";
    private ServerSocket serverSocket;

    private ExecutorService executorService;
    private Map<Integer, Writer> ChatServerMap;

    public ChatServer(){
        executorService = Executors.newFixedThreadPool(10);
        ChatServerMap = new HashMap<>();
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if(socket!=null){
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ChatServerMap.put(socket.getPort(),writer);
            System.out.println("客户端["+port+"]已经连接到服务器");
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if(socket!=null){
            int port = socket.getPort();
            if(ChatServerMap.containsKey(port)){
                ChatServerMap.get(port).close();
            }
            ChatServerMap.remove(port);
            System.out.println("客户端["+port+"]已经断开");
        }
    }

    public void forwardMessage(Socket socket,String msg) throws IOException {
        for (Integer id:ChatServerMap.keySet()
             ) {
            if(!id.equals(socket.getPort())){
                Writer writer = ChatServerMap.get(id);
                writer.write(msg);
                writer.flush();
            }
        }
    }

    public boolean readyToExit(String msg){
        return msg.equals(QUIT);
    }

    public synchronized void close(){
        if(serverSocket!=null){
            try {
                serverSocket.close();
                System.out.println("关闭Server端");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        //绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAUT_PORT);
            System.out.println("启动服务器监听端口"+DEFAUT_PORT+"...");
            while(true){
                Socket socket = serverSocket.accept();
                //创建ChatHandler线程
                executorService.execute(new ChatHandler(this,socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
