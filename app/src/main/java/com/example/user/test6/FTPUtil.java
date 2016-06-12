package com.example.user.test6;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by user on 2016/6/12.
 */
public class FTPUtil {
    private FTPClient ftpClient;
    private int reply;

    public void connectServer() throws IOException {
        if (!(ftpClient!=null&&ftpClient.isConnected())) {
            ftpClient = new FTPClient();
            Log.e("downloadFromVidicon", "开始连接");
            ftpClient.connect("192.168.15.1", 21);
            reply = ftpClient.getReplyCode();
            Log.e("连接", "返回码为:" + reply);
//        ftpClient.setControlEncoding("GBK");
            ftpClient.login("root", "12345678");
            reply = ftpClient.getReplyCode();
            Log.e("登陆", "登陆返回码为:" + reply);
            ftpClient.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                Log.e("退出", "");
                return;
            }
        }
    }

    public void closeServer() throws IOException {
        if (ftpClient != null && ftpClient.isConnected()) {
            ftpClient.logout();//退出FTP服务器   
            ftpClient.disconnect();//关闭FTP连接   
        }
    }

    public void downloadTitleList(List<String> titleList) throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.changeWorkingDirectory("/SD/NORMAL");
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile f : ftpFiles) {
                String filrName = f.getName();
                titleList.add(filrName);
            }

        }


    }

    public synchronized Bitmap  downloadBitmap(String fileName) throws IOException {
        Bitmap bitmap = null;
        if (ftpClient.isConnected()) {
            ftpClient.changeWorkingDirectory("/SD/THUMB");
            reply = ftpClient.getReplyCode();
            InputStream in = ftpClient.retrieveFileStream(fileName);
//            InputStream in = ftpClient.retrieveFileStream(fileName);
            bitmap = BitmapFactory.decodeStream(in);
        }
        return bitmap;
    }
}

