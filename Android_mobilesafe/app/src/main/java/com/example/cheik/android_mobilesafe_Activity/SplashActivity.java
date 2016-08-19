package com.example.cheik.android_mobilesafe_Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.example.cheik.android_mobilesafe.R;
import com.example.cheik.android_mobilesafe_Utils.StreamUtils;
import com.example.cheik.android_mobilesafe_Utils.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    protected static final String tag = "SplashActivity";
    /*更新版本的状态吗*/
    protected static final int UPDATE_VERSION = 100;
    /*进入应用程序主界面的状态吗*/
    protected static final int ENTER_HOME = 101;
    /*url地址出错的状态吗*/
    protected static final int URL_ERROR = 102;
    protected static final int IO_ERROR = 103;
    protected static final int JSON_ERROR = 104;

    private TextView textView;
    private int mLocalVersionCode;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_VERSION:
                    //提示用户更新
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    //进入应用主程序
                    enterHome();
                    break;
                case URL_ERROR:
                    ToastUtil.show(getApplicationContext(), "url异常");
                    enterHome();
                    break;
                case IO_ERROR:
                    ToastUtil.show(getApplicationContext(), "读取异常");
                    enterHome();
                    break;
                case JSON_ERROR:
                    ToastUtil.show(getApplicationContext(), "json解析异常");
                    enterHome();
                    break;
                default:
                    enterHome();
                    break;
            }

        }
    };

    private void downloadApk() {
        //apk下载地址,放置apk 的所有路径
        //1,判断SD卡是否可以
        Log.i(tag,"downloadApk");
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //2,获取sd路径
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator+"mobilesafe.apk";
            //3,发送请求,获取apk,并放置到指定的位置
            HttpUtils httpUtils = new HttpUtils();
            //4,发送请求,传递参数(下载位置,下载应用放置位置)
            httpUtils.download("http://www.gwfx.com/download/GTS2.apk?v=20160803", path, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    //下载成功(下载过后的放置在SD卡中的apk)
                    Log.i(tag,"下载成功");
                    File file = responseInfo.result;
                    //提示用户安装
                    installApk(file);
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    //下载失败
                    Log.i(tag,"下载失败");
                    enterHome();
                }

                @Override
                public void onStart(){
                    Log.i(tag,"刚刚下载");
                    super.onStart();
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                   Log.i(tag,"下载中....."+"total="+total+"current="+current);
                    super.onLoading(total, current, isUploading);
                }
            });
        }
    }

    private void installApk(File file) {
    }

    private void enterHome() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        finish();
        Log.i(tag,"enterHome");
    }

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
    private void showUpdateDialog() {
        //对话框是依赖activity存在
        Log.i(tag,"showUpdateDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置左上角的图表
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("版本更新");
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //下载apk,apk链接地址,downloadUrl
                downloadApk();
            }
        });

        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //取消对话框,进入主界面
                enterHome();
            }
        });


        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //即使用户点击取消,也需要让其进入应用程序主界面
                enterHome();
                dialogInterface.dismiss();
            }
        });
        builder.show();
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

        new Thread(){
            public void run (){
                //发送通知对象
                final Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();
                try {
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
                        //解析
                        JSONObject jsonObject = new JSONObject(json);
//                        String data = jsonObject.getString("data");
                        JSONObject data = jsonObject.getJSONObject("data");
                        String version = data.getString("version");
                        Log.i(tag,version);
                        //对比版本号
                        if(mLocalVersionCode < Integer.parseInt(version)){
                            msg.what = UPDATE_VERSION;//弹出提示框
                            Log.i(tag,"弹出提示框");
                        }else{
                            msg.what = ENTER_HOME;//进入主界面
                            Log.i(tag,"进入主界面");
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    msg.what = URL_ERROR;
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = IO_ERROR;
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg.what = JSON_ERROR;
                }finally {
                    //指定睡眠时间,请求网络的时长超过4秒则不做处理
                    //请求网络的时长小于4秒,强制让其睡眠满4秒钟
                    long endTime = System.currentTimeMillis();
                    if(endTime-startTime<4000){
                        try {
                            Thread.sleep(4000-(endTime-startTime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(msg);
                }

            }

        }.start();
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
