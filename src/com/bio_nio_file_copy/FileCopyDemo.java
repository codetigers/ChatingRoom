package com.bio_nio_file_copy;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.BatchUpdateException;

/**
 * @program: ChatingRoom
 * @description:
 * @author: Mr.li
 * @create: 2020-07-10 08:43
 **/
public class FileCopyDemo {

    public static void close(Closeable close){ //只要是可关闭的都可以
        if(close!=null){
            try {
                close.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void test(FileCopyRunner test,File source,File target){
        test.copyFile(source,target);
    }

    public static void main(String[] args) {

        FileCopyRunner noBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new FileInputStream(source);
                    fout = new FileOutputStream(target);
                    int result = 0;
                    while((result = fin.read())!=-1){
                        fout.write(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
        };

        FileCopyRunner bufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                BufferedInputStream fin = null;
                BufferedOutputStream fout = null;
                try {
                    fin = new BufferedInputStream(new FileInputStream(source));
                    fout = new BufferedOutputStream(new FileOutputStream(target));
                    byte[] buffer = new byte[1024];
                    int result = 0;
                    while((result = fin.read(buffer)) != -1){//read返回所读入的字节数量
                        fout.write(buffer,0,result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
        };

        FileCopyRunner nioBufferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;
                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int result = 0;
                    while(fin.read(buffer)!=-1){
                        buffer.flip();
                        while(buffer.hasRemaining()){
                            fout.write(buffer);
                        }
                        buffer.clear();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
        };

        FileCopyRunner nioTransferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;
                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();
                    long size = fin.size();
                    long transTo = 0;
                    while(size!=transTo){
                        transTo += fin.transferTo(0,size,fout);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
        };

        File source = new File("D:\\学习资料\\速盘极速版激活码.txt");
        File target = new File("D:\\学习资料\\速盘极速版激活码1.txt");

        //test(noBufferStreamCopy,source,target);
        //test(bufferStreamCopy,source,target);
        //test(nioBufferCopy,source,target);
        test(nioTransferCopy,source,target);

    }
}
