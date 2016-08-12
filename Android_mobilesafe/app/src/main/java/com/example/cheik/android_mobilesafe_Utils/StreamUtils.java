package com.example.cheik.android_mobilesafe_Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Cheik on 16/8/12.
 */
public class StreamUtils {
    public static String streamToString(InputStream stream) {
        //1,在读取的过程中,将读取的内容存储值缓存中,然后一次性的转换成字符串返回
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //2,读流操作
        byte[] buffer = new byte[1024];
        //3,记录读取数据的临时变量
        int temp = -1;
        try {
            while ((temp = stream.read(buffer)) != -1){
                bos.write(buffer,0,temp);
                //返回读取的数据
                return bos.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                stream.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return  null;
    }
}
