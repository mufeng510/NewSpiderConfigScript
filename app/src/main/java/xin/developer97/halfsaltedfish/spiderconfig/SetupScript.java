package xin.developer97.halfsaltedfish.spiderconfig;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetupScript extends AppCompatActivity {
    Tools tools = Tools.getTools();
    String baseUrl = "http://" + tools.host + "/KingCardServices/tinyscript/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup_script);
        final RadioGroup scriptGroup = (RadioGroup)findViewById(R.id.scriptGroup);
        final EditText script = (EditText)findViewById(R.id.script);
        Button moudleSet = (Button)findViewById(R.id.moudleSet);
        Button setupScript = (Button)findViewById(R.id.setupScript);
        Button unstallScript = (Button)findViewById(R.id.unstallScript);

        SharedPreferences sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        //自定义壁纸
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layoutSetupScripy);
        if (sp.getString("backpath",null)!= null){
            String uri = sp.getString("backpath",null);
            Drawable drawable= (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        }else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }

        //获取选中脚本
        scriptGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radbtn = (RadioButton) findViewById(checkedId);
                script.setText(radbtn.getText());
            }
        });
        //安装脚本
        setupScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(script.getText().length()<1){
                    tools.mes("请先选择或输入要安装的脚本");
                }else {
                    try {
                        tools.stop();
                        Thread.sleep(2000);
                        tools.delete();
                        String path = "";
                        switch (script.getText().toString()){
                            case "Baymin":
                                path = "Baymin.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "Leaves":
                                path = "Leaves.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "Jume":
                                path = "Jume.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "JJ":
                                path = "JJ.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "JJ-MTK":
                                path = "JJ-MTK.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "ZJL1.9":
                                path = "ZJL1.9.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "ZJL2.0bate16":
                                path = "ZJL2.0bate16.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "ZJL2.0bata12":
                                path = "ZJL2.0bata12.zip";
                                editor.putBoolean("openTask", true);
                                break;
                            case "sussr":
                                path = "sussr.zip";
                                editor.putBoolean("openTask", false);
                                break;
                            case "SSRR5.3":
                                path = "SSRR5.3.zip";
                                editor.putBoolean("openTask", false);
                                break;
                        }
                        path = baseUrl + path;
                        editor.commit();
                        downloadScript(v,path);
                    }catch (Exception e){
                        e.printStackTrace();
                        tools.mes("请检查文件名是否正确或网络连通性");
                    }
                }

            }
        });
        //卸载脚本
        unstallScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.delete();
            }
        });
        //模板设置
        moudleSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupScript.this, MoudleSet.class);
                startActivity(intent);
            }
        });
    }
    //获取app存储路径
    private String getAppPath(String filePath){
        String pattern = "(.*?)/files";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(filePath);
        if(m.find()){
            String appPath = m.group(1);
            return appPath;
        }
        else return "/data/user/0/xin.developer97.halfsaltedfish.spiderconfig";
    }

    //获取网络文件
    public void downloadScript(View v,String path) {
        //1). 主线程, 显示提示视图: ProgressDialog
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        //准备用于保存APK文件的File对象
        final File zipFile = new File(getApplicationContext().getFilesDir().getAbsolutePath(), "script.zip");
        System.out.println(zipFile.getAbsolutePath());

        //2). 启动分线程, 请求下载APK文件, 下载过程中显示下载进度
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //1. 得到连接对象
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //2. 设置
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    connection.setRequestProperty("Accept-Encoding","gzip, deflate");
                    //3. 连接
                    connection.connect();
                    //4. 请求并得到响应码200
                    int responseCode = connection.getResponseCode();
                    if(responseCode==200) {
                        Thread.sleep(500);
                        //设置dialog的最大进度
                        dialog.setMax(connection.getContentLength());


                        //5. 得到包含文件数据的InputStream
                        InputStream is = connection.getInputStream();
                        //6. 创建指向文件的FileOutputStream
                        FileOutputStream fos = new FileOutputStream(zipFile);
                        //7. 边读边写
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while((len=is.read(buffer))!=-1) {
                            fos.write(buffer, 0, len);
                            //8. 显示下载进度
                            dialog.incrementProgressBy(len);

                            //休息一会(模拟网速慢)
                            //Thread.sleep(50);
                            SystemClock.sleep(5);
                        }

                        fos.close();
                        is.close();
                    }
                    //9. 下载完成, 关闭, 进入3)
                    connection.disconnect();
                    tools.unzip(zipFile.getPath(),getApplicationContext().getFilesDir().getAbsolutePath());
                    Thread.sleep(2000);
                    String appPath = getApplicationContext().getFilesDir().getAbsolutePath();
                    tools.execShell("chmod -R 777 "+ appPath);
                    setupfail();
                    //3). 主线程, 移除dialog, 启动安装
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void setupfail(){
        // path为指定目录
        String path = getFilesDir() + "/tools/";
        File dir = new File(path);
        if(!dir.exists())dir.mkdir();
        String[] necessaryFile = {"curl","tcpdump.bin"};
        for(String s:necessaryFile){
            File file = new File(path+s);
            if (!file.exists()) {
                tools.copyFile(s,path);
            }
        }
        //tiny.conf
        File file = new File(getFilesDir() +"/tiny.conf");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tools.mes("ok了");
    }
}
