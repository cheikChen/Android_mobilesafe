package com.example.cheik.android_mobilesafe;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

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
//        checkVersion();
    }

    private void checkVersion() {
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
