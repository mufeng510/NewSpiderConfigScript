package xin.developer97.halfsaltedfish.spiderconfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ddz.floatingactionbutton.FloatingActionButton;
import com.ddz.floatingactionbutton.FloatingActionMenu;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.vondear.rxtool.RxShellTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    Tools tools = Tools.getTools();
    private static TextView updateTime,text;
    private static SharedPreferences sp;
    private static Handler mHandler;
    Intent intent_service;
    static String versionName_new = "查询失败";
    static android.os.CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        intent_service = new Intent(this, MyService.class);
        tools.hideInRecents();
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };


        if(!tools.isNotificationEnabled()) ToastUtils.show("为了更好的体验，建议开启通知栏权限");

        //自定义壁纸
        RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.layoutmain);
        if (sp.getString("backpath", null) != null) {
            String uri = sp.getString("backpath", null);
            Drawable drawable = (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        } else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }

        //控件
        FloatingActionMenu fam1 = (FloatingActionMenu)findViewById(R.id.fam1);
        FloatingActionButton modle = (FloatingActionButton) findViewById(R.id.modle);
        FloatingActionButton AntiJump = (FloatingActionButton) findViewById(R.id.AntiJump);
        FloatingActionButton check = (FloatingActionButton) findViewById(R.id.check);
        FloatingActionButton speedtest = (FloatingActionButton) findViewById(R.id.speedtest);
        FloatingActionButton thawBrowser = (FloatingActionButton) findViewById(R.id.thawBrowser);
        FloatingActionButton red = (FloatingActionButton) findViewById(R.id.red);
        ImageButton scriptO = (ImageButton) findViewById(R.id.scriptO);
        ImageButton getweb = (ImageButton) findViewById(R.id.getweb);
        ImageButton get_packet = (ImageButton) findViewById(R.id.get_packet);
        ImageButton scriptC = (ImageButton) findViewById(R.id.scriptC);
        FloatingActionMenu fam2 = (FloatingActionMenu)findViewById(R.id.fam2);
        FloatingActionButton scriptManagement = (FloatingActionButton) findViewById(R.id.ScriptManagement);
        FloatingActionButton reward = (FloatingActionButton) findViewById(R.id.reward);
        FloatingActionButton group = (FloatingActionButton) findViewById(R.id.group);
        FloatingActionButton set = (FloatingActionButton) findViewById(R.id.set);
        updateTime = (TextView) findViewById(R.id.updateTime);
        text = (TextView) findViewById(R.id.text);
        //root权限
        try {
            String apkRoot = "chmod 777 " + getPackageCodePath();
            SystemManager.RootCommand(apkRoot);
        }catch (Exception e){
            tools.mes("未获取到Root权限,请确保授权管理在后台");
        }

        View view = this.getWindow().getDecorView();
        //未设置提示
        if (!sp.getBoolean("doset", false)) {
            AlertDialog alert = null;
            AlertDialog.Builder builder = null;
            builder = new AlertDialog.Builder(MainActivity.this);
            alert = builder.setTitle("声明")
                    .setMessage("点击确定申请必要的权限\n存储权限:用于写入模式\n通知权限:用于显示通知栏快捷工具\n悬浮窗权限:用于消息提示\n\n本软件完全免费，仅供娱乐使用，切勿用于非法用途！造成的一切后果与开发者无关！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //动态权限申请
                            requestPermission(view);
                            Intent intent = new Intent(MainActivity.this, set.class);
                            startActivity(intent);
                        }
                    }).create();             //创建AlertDialog对象
            alert.show();
        }else isHasPermission(view);
        //检查版本更新
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        //获取本地版本号
                        int versionCode = 59;
                        try {
                            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        //获取最新版本号
                        HttpURLConnection con=null;
                        String path="http://" + getApplicationContext().getString(R.string.host) +"/KingCardServices/get_version.php";
                        try {
                            URL url = new URL(path);
                            con= (HttpURLConnection) url.openConnection();
                            con.setDoInput(true);
                            con.setDoOutput(true);
                            con.setUseCaches(false);
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Connection", "keep-alive");
                            con.setRequestProperty("contentType", "application/json");

                            con.connect();

                            OutputStream out = con.getOutputStream();
                            // 写入请求的字符串
                            out.write((getPackageName()).getBytes("utf-8"));
                            out.flush();
                            out.close();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String lines;
                            StringBuffer sbf = new StringBuffer();
                            while ((lines = reader.readLine()) != null) {
                                lines = new String(lines.getBytes(), "utf-8");
                                sbf.append(lines);
                            }
                            String versionJson = sbf.toString();
                            Log.i("versionJson",versionJson);
                            if (versionJson != "{\"success\":0,\"message\":\"No products found\"}") {
                                try {
                                    JSONObject versionText = new JSONObject(versionJson);
                                    int versionCode_new = versionText.getInt("versionCode");
                                    versionName_new = versionText.getString("versionName");
                                    String updataText = versionText.getString("updataText");
//                                    System.out.println(updataText);
                                    if(versionCode<versionCode_new){
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog alert = null;
                                                AlertDialog.Builder builder = null;
                                                builder = new AlertDialog.Builder(MainActivity.this);
                                                alert = builder.setTitle("有新版本"+versionName_new)
                                                        .setMessage(updataText)
                                                        .setNegativeButton("取消",null)
                                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent();
                                                                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D500dbsykLTTnkUpBTJg97HcVZH5yzpfB" ));
                                                                try {
                                                                    startActivity(intent);
                                                                } catch (Exception e) {
                                                                    tools.mes("未安装手Q或安装的版本不支持");
                                                                }
                                                            }
                                                        }).create();             //创建AlertDialog对象
                                                alert.show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
        //启动服务
        if(sp.getBoolean("openService",true)){
            MyService.notFirstRun = false;
            startService(intent_service);
        }


        //模式编辑
        modle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                Intent intent = new Intent(MainActivity.this, Modle.class);
                startActivity(intent);
            }
        });
        //防跳编辑
        AntiJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                Intent intent = new Intent(MainActivity.this, AntiJump.class);
                startActivity(intent);
            }
        });
        //检测状态
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                tools.detection();
            }
        });
        // 网速测试
        speedtest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent);
            }
        });
        //解冻QQ浏览器
        thawBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                RxShellTool.execCmd(new String[]{"pm enable com.tencent.mtt","am start -n com.tencent.mtt/.MainActivity"},true);
            }
        });
        //红包码
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                tools.copy("528207543");
                tools.mes("复制成功，请在支付宝中粘贴搜索");
                try {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.eg.android.AlipayGphone");
                    if (intent != null) {
                        intent.putExtra("type", "110");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    tools.mes("无法打开支付宝");
                }
            }
        });

        //开启脚本
        scriptO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.open();
            }
        });
        //获取服务器配置
        getweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.getConfig();
            }
        });
        // 抓包
        get_packet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.autopull();
            }
        });
        //关闭脚本
        scriptC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.closeTimedTask();
                tools.stop();
            }
        });

        //设置
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam2.collapse();
                Intent intent = new Intent(MainActivity.this, set.class);
                startActivity(intent);
            }
        });
        //捐赠
        reward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam2.collapse();
                Intent intent = new Intent(MainActivity.this, Reward.class);
                startActivity(intent);
            }
        });
        //安装脚本
        scriptManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam2.collapse();
                Intent intent = new Intent(MainActivity.this, SetupScript.class);
                startActivity(intent);
            }
        });
        // 一键加群
        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam2.collapse();
                Intent intent = new Intent();
                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D500dbsykLTTnkUpBTJg97HcVZH5yzpfB"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    tools.mes("未安装手Q或安装的版本不支持");
                }
            }
        });


    }

    //更新ui
    public static void updataUI(final int time, final String addStr){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(sp.getString("dynamic","QQ").equals("QQ")){
                    if(timer!=null)timer.cancel();
                    updateTime.setEnabled(false);
                    timer = new android.os.CountDownTimer(time*60000, 60000) {

                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onTick(long millisUntilFinished) {
                            updateTime.setText(String.format("剩余 %d 分钟", millisUntilFinished / 60000));
                        }

                        @Override
                        public void onFinish() {
                            updateTime.setEnabled(true);
                            updateTime.setText("已过期");
                        }
                    };
                    timer.start();
                }
                text.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date( ))+addStr);
            }
        });
    }

    @Override
    public void onBackPressed() {


        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("退出提示");
        dialog.setMessage("您确定退出应用吗?");
        dialog.setNegativeButton("取消",null);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopService(intent_service);
                finish();
                System.exit(0);
                tools.execShell(getFilesDir() + "/tools/" + "am force-stop "+getPackageName());
            }
        });
        dialog.show();

    }
    public void requestPermission(View view) {
        XXPermissions.with(this)
                // 可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .constantRequest()
                // 不指定权限则自动获取清单中的危险权限
                .permission(Permission.Group.STORAGE)
                .permission(Permission.SYSTEM_ALERT_WINDOW)
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            ToastUtils.show("获取权限成功");
                        }else {
                            ToastUtils.show("获取权限成功，部分权限未正常授予");
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            ToastUtils.show("被永久拒绝授权，请手动授予权限");
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(MainActivity.this);
                        }else {
                            ToastUtils.show("获取权限失败");
                        }
                    }
                });
    }

    public void isHasPermission(View view) {
        if (XXPermissions.isHasPermission(MainActivity.this, Permission.Group.STORAGE)) {
        }else {
            ToastUtils.show("还没有获取到存储权限或者部分权限未授予,请授予");
            requestPermission(view);
        }
        if (XXPermissions.isHasPermission(MainActivity.this, Permission.SYSTEM_ALERT_WINDOW)) {
        }else {
            ToastUtils.show("还没有获取到悬浮窗权限,请授予");
            requestPermission(view);
        }
    }
}
