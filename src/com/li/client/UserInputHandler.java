package com.li.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @program: ChatingRoom
 * @description: 用户输入线程，通过此线程输入到服务器，判断是否关闭写
 * @author: Mr.li
 * @create: 2020-07-08 15:16
 **/
public class UserInputHandler implements Runnable{

    private ChatClient chatClient;

    public UserInputHandler(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @Override
    public void run() {
        //读取用户输入
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            try {
                String input = consoleReader.readLine();
                chatClient.send(input);
                if(chatClient.readyToQuit(input)){
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
