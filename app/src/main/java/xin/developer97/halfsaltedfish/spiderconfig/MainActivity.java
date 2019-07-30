package xin.developer97.halfsaltedfish.spiderconfig;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.ddz.floatingactionbutton.FloatingActionButton;
import com.ddz.floatingactionbutton.FloatingActionMenu;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements  android.view.GestureDetector.OnGestureListener{

    Tools tools = new Tools();
    private static TextView updateTime,text;
    SharedPreferences sp;
    private static Handler mHandler;
    GestureDetector gd;
    Intent intent_service;
    static String versionName_new = "查询失败";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        tools.setContext(getApplicationContext());
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        intent_service = new Intent(this, MyService.class);
        tools.hideInRecents();
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        //创建手势检测器
        gd = new GestureDetector(this,this);
        //动态权限申请
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.REORDER_TASKS
        };
        // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
        List<String> mPermissionList = new ArrayList<>();

        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (!mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            String[] permission = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permission, 1);
        }

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
        FloatingActionButton get_packet = (FloatingActionButton) findViewById(R.id.get_packet);
        FloatingActionButton red = (FloatingActionButton) findViewById(R.id.red);
        ImageButton scriptO = (ImageButton) findViewById(R.id.scriptO);
        ImageButton getweb = (ImageButton) findViewById(R.id.getweb);
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

        //未设置提示
        if (!sp.getBoolean("doset", false)) {
            AlertDialog alert = null;
            AlertDialog.Builder builder = null;
            builder = new AlertDialog.Builder(MainActivity.this);
            alert = builder.setTitle("声明")
                    .setMessage("请在设置中完成相关设置,点击确定自动清除旧脚本，请重新安装\n\n本软件完全免费，仅供娱乐使用，切勿用于非法用途！造成的一切后果与开发者无关！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, set.class);
                            startActivity(intent);
                        }
                    }).create();             //创建AlertDialog对象
            alert.show();
        }
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
        // 抓包
        get_packet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                Intent intent = new Intent(MainActivity.this, GetPacket.class);
                startActivity(intent);
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
                tools.getConfig(true);
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
    public static void updataUI(final String time, final String config){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateTime.setText(time);
                text.setText(config);
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float minMove = 120;         //最小滑动距离
        float minVelocity = 0;      //最小滑动速度
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if(beginX-endX>minMove&&Math.abs(velocityX)>minVelocity){   //左滑
            Intent intent = new Intent(MainActivity.this, GetPacket.class);
            startActivity(intent);
//            Toast.makeText(this,velocityX+"左滑",Toast.LENGTH_SHORT).show();
        }
//        else if(endX-beginX>minMove&&Math.abs(velocityX)>minVelocity){   //右滑
//            Toast.makeText(this,velocityX+"右滑",Toast.LENGTH_SHORT).show();
//        }else if(beginY-endY>minMove&&Math.abs(velocityY)>minVelocity){   //上滑
//            Toast.makeText(this,velocityX+"上滑",Toast.LENGTH_SHORT).show();
//        }else if(endY-beginY>minMove&&Math.abs(velocityY)>minVelocity){   //下滑
//            Toast.makeText(this,velocityX+"下滑",Toast.LENGTH_SHORT).show();
//        }

        return false;
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
}
