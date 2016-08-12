package com.example.cheik.android_mobilesafe_Activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.example.cheik.android_mobilesafe.R;
import com.example.cheik.android_mobilesafe_Utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    protected static final String tag = "SplashActivity";
    private TextView textView;

    private int mLocalVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉头部bar
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);

        initUI();
        initData();


    }
    /*初始化数据*/
    private void initData() {
        //获取版本名称
        textView.setText("版本名称:" + getVersionName());
        //获取本地版本号
        mLocalVersionCode = getVersionCode();
        //和服务器版本进行比较
        checkVersion();

    }

    private void checkVersion() {
        System.out.println("hhhhh");
        Log.i(tag, "fdfdfd");
        new Thread(){
            public void run (){
                try {
                    System.out.println("dddddd");
//                    Log.i(tag, "fdfdfd");
                    URL url = new URL("http://113.28.105.81:3000/queryVersion?company_id=1");
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setConnectTimeout(2000);
                    connection.setReadTimeout(2000);
                    //请求成功
                    if (connection.getResponseCode() == 200) {
                        //已流的形式将数据保存下来
                        InputStream stream = connection.getInputStream();
                        //将流转换成字符串
                        String json = StreamUtils.streamToString(stream);
                        Log.i(tag, json);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        };
    }


    private int getVersionCode() {
        PackageManager pm = getPackageManager();

        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(),0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private String getVersionName() {
        //1,包管理对象
        PackageManager pm = getPackageManager();
        //2,从包管理对象获取基本信息
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(),0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*初始化UI方法*/
    private void initUI() {
        textView = (TextView) findViewById(R.id.tv_version_name);
    }


}
