package com.whx.dataoperationtest.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by whx on 2017/4/11.
 */

public class DownloadThread extends Thread{
    private static final String TAG = "DownloadThread";
    private File saveFile;                      //下载的数据保存到的文件
    private URL url;                            //下载的URL
    private int block;                          //每条线程下载的大小
    private int threadId = -1;                  //初始化线程id设置
    private int downloadSize;                   //该线程已下载的数据长度
    private boolean finish = false;             //该线程是否完成下载的标志
    private FileDownloader fileDownloader;      //文件下载器
    private static final int TIME_OUT = 5*1000;

    public DownloadThread(FileDownloader fileDownloader, URL url, File file,
                          int block, int downloadSize, int threadId) {
        this.fileDownloader = fileDownloader;
        this.url = url;
        this.saveFile = file;
        this.block = block;
        this.downloadSize = downloadSize;
        this.threadId = threadId;

    }
    @Override
    public void run() {
        if(downloadSize < block) {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(TIME_OUT);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, " +
                        "application/x-shockwave-flash, application/xaml+xml, " +
                        "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                        "application/x-ms-application, application/vnd.ms-excel, " +
                        "application/vnd.ms-powerpoint, application/msword, */*");
                conn.setRequestProperty("Accept-Language", "zh-CN");
                conn.setRequestProperty("Referer", url.toString());
                conn.setRequestProperty("Charset", "UTF-8");

                int startPos = block*(threadId - 1) + downloadSize;     //开始位置
                int endPos = block * threadId - 1;                      //结束位置

                conn.setRequestProperty("Range", "bytes=" + startPos + "-"+ endPos);//设置获取实体数据的范围
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                        "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; " +
                        ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                conn.setRequestProperty("Connection", "Keep-Alive");

                InputStream inputStream = conn.getInputStream();
                byte[] buffer = new byte[1024];
                int offset = 0;
                print("Thread " + this.threadId + " start download from position "+ startPos);  //打印该线程开始下载的位置

                RandomAccessFile threadFile = new RandomAccessFile(this.saveFile, "rwd");
                threadFile.seek(startPos);

                while(!fileDownloader.getExited() && (offset = inputStream.read(buffer, 0, 1024)) != -1) {
                    threadFile.write(buffer, 0, offset);
                    downloadSize += offset;

                }

                if(fileDownloader.getExited()) {
                    fileDownloader.update(this.threadId, downloadSize); //把该线程已经下载的数据长度更新到数据库和内存哈希表中
                    fileDownloader.append(offset);            //把新下载的数据长度加入到已经下载的数据总长度中
                }
                threadFile.close();
                inputStream.close();
                print("Thread " + this.threadId + " download finish");
                this.finish = true;                               //设置完成标记为true,无论下载完成还是用户主动中断下载
            } catch (IOException e) {
                this.downloadSize = -1;
                e.printStackTrace();
            }
        }
    }
    private static void print(String info) {
        Log.i(TAG, info);
    }
    public boolean isFinish() {
        return finish;
    }
    public long getDownloadSize() {
        return downloadSize;
    }
}
