package com.AIOserver;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @program: ChatingRoom
 * @description:
 * @author: Mr.li
 * @create: 2020-07-14 20:45
 **/
public class client {
    final String LOCALHOST = "localhost";
    final int DEFAULTPORT = 8888;
    AsynchronousSocketChannel clientChannel;

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

    public void start(){
        try {
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(LOCALHOST,DEFAULTPORT));
            future.get();

            //等待用户输入
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                String input = consoleReader.readLine();
                byte[] inputBytes = input.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(inputBytes);
                Future<Integer> writeResult = clientChannel.write(buffer);
                writeResult.get();
                buffer.flip();
                Future<Integer> readResult = clientChannel.read(buffer);
                readResult.get();
                String echo = new String(buffer.array());
                buffer.clear();
                System.out.println(echo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }finally {
            close(clientChannel);
        }
    }

    public static void main(String[] args) {
        client client = new client();
        client.start();
    }
}
