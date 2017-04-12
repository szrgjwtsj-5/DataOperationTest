package com.whx.dataoperationtest.down;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.whx.dataoperationtest.R;
import com.whx.dataoperationtest.util.FileDownloader;

import java.io.File;


/**
 * Created by whx on 2017/4/12.
 */

public class DownloadActivity extends AppCompatActivity{

    private static final int PROCESSING = 0x123;
    private static final int FAILURE = 0x456;
    private static final int THREAD_SIZE = 3;
    private File saveFile;

    private Button startBtn, stopBtn;
    private EditText urlText;
    private ProgressBar progressBar;
    private TextView textResult;

    private FileDownloader downloader;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == PROCESSING) {
                int size = msg.getData().getInt("size");
                progressBar.setProgress(size);
                //计算已经下载的百分比,此处需要转换为浮点数计算
                float num = (float)progressBar.getProgress() / (float)progressBar.getMax();
                int result = (int)(num * 100);     //把获取的浮点数计算结果转换为整数
                textResult.setText(result+ "%");   //把下载的百分比显示到界面控件上
                if(progressBar.getProgress() == progressBar.getMax()){ //下载完成时提示
                    Toast.makeText(getApplicationContext(), "文件下载成功", Toast.LENGTH_SHORT).show();
                }
            }
            if(msg.what == FAILURE) {
                Toast.makeText(getApplicationContext(), "文件下载失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.download_test);

        initView();
    }
    private void initView() {
        startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(new ClickListener());

        stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setOnClickListener(new ClickListener());

        urlText = (EditText) findViewById(R.id.url_text);
        progressBar = (ProgressBar) findViewById(R.id.download_progress);
        textResult = (TextView) findViewById(R.id.progress_text);

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            filePath += "dwn";

            saveFile = new File(filePath);
        }else{
            Toast.makeText(getApplicationContext(), "sd卡读取失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void download(String url, File saveFile) {

        downloader = new FileDownloader(getApplicationContext(), url, saveFile, THREAD_SIZE);
        progressBar.setMax(downloader.getFileSize());

        downloader.registerListener(new FileDownloader.ProgressListener() {
            @Override
            public void onDownloadSize(int downloadedSize) {
                Message msg = Message.obtain();
                msg.what = PROCESSING;
                msg.getData().putInt("size", downloadedSize);
                handler.sendMessage(msg);
            }
        });

        try {
            downloader.download();
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendMessage(handler.obtainMessage(FAILURE));
        }
    }
    private void stop() {
        if(downloader != null) downloader.stop();
    }

    @Override
    protected void onDestroy() {
        downloader.unregisterListener();
        super.onDestroy();
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start:
                    download(urlText.getText().toString(), saveFile);
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(true);
                    break;
                case R.id.stop:
                    stop();
                    startBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                    break;
            }
        }
    }
}
