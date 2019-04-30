package com.zebucars.alioss.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

/**
 * @author 罗富清
 * @date 2019/4/25
 */
public class IoUtils {

    /**
     * 读出文件
     *
     * @param file file
     */
    public static String readFile(File file) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        String str = null;
        try {
            InputStream is = new FileInputStream(file);
            int len;
            while ((len = is.read()) != -1) {
                bao.write(len);
            }
            str = bao.toString();
            is.close();
            bao.close();
            return str;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return str;
        } catch (IOException e) {
            e.printStackTrace();
            return str;
        }
    }

    /**
     * 写入文件
     *
     * @param file file
     * @param str  写入内容
     */
    public static boolean writeFile(String str, File file) {
        try {
            if (!file.exists()) {
                // 创建文件
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // 向文件写入内容(输出流)
        byte bt[] = str.getBytes();
        try {
            FileOutputStream in = new FileOutputStream(file);
            try {
                in.write(bt, 0, bt.length);
                in.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 添加一行代码
     * @param fileUrl file
     * @param content content
     */
    public static void writeLine(String fileUrl, String content) {
        File file = new File(fileUrl);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            out.write(content + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
