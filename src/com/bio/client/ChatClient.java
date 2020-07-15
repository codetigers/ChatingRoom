package com.bio.client;

import java.io.*;
import java.net.Socket;

/**
 * @program: ChatingRoom
 * @description: 客户端(发送消息，连接，接收消息，关闭资源，判断退出，开启)
 * @author: Mr.li
 * @create: 2020-07-08 15:15
 **/
public class ChatClient {
    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final int DEFAULT_SERVER_PORT = 8888;
    private final String QUIT = "quit";

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    //发送消息
    public void send(String msg) throws IOException {
        if(!socket.isOutputShutdown()){
            writer.write(msg+"\n");
            writer.flush();
        }
    }

    public String receive() throws IOException {
        String msg = null;
        if(!socket.isInputShutdown()){
            msg = reader.readLine();
        }
        return msg;
    }
    public boolean readyToQuit(String msg){
        return msg.equals(QUIT);
    }

    public void close(){
        if(writer!=null){
            try {
                System.out.println("客户端["+socket.getPort()+"]退出");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void start(){
        try {
            socket = new Socket(DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //处理用户输入
            new Thread(new UserInputHandler(this)).start();
            //读取用户转发的信息
            String msg = null;
            while((msg=receive())!=null){
                System.out.println(msg+"\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
