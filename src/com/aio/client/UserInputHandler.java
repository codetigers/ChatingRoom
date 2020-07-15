package com.aio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @program: ChatingRoom
 * @description:
 * @author: Mr.li
 * @create: 2020-07-15 16:01
 **/
public class UserInputHandler implements Runnable {
    ChatClient client;
    public UserInputHandler(ChatClient chatClient) {
        this.client=chatClient;
    }
    @Override
    public void run() {
        BufferedReader read=new BufferedReader(
                new InputStreamReader(System.in)
        );
        while (true){
            try {
                String input=read.readLine();
                client.send(input);
                if(input.equals("quit"))
                    break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}