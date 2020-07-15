package com.bio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @program: ChatingRoom
 * @description:用户转发消息,以及判断服务器是否退出的线程
 * @author: Mr.li
 * @create: 2020-07-08 15:10
 **/
public class ChatHandler implements Runnable{
    private ChatServer server;
    private Socket socket;
    public ChatHandler(ChatServer server,Socket socket){
        this.server = server;
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            //存储新上线用户
            server.addClient(socket);
            //读取用户发送的信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = null;
            while((msg = reader.readLine())!=null){
                String fwdMsg = "客户端["+socket.getPort()+"]:"+msg+"\n";
                System.out.print(fwdMsg);
                server.forwardMessage(socket,fwdMsg);
                //检查用户是否退出
                if(server.readyToExit(msg)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
