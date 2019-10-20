package xin.developer97.halfsaltedfish.spiderconfig;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.hjq.toast.ToastUtils;
import com.hjq.xtoast.XToast;
import com.vondear.rxtool.RxShellTool;
import com.vondear.rxtool.RxTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.Process;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;
import static android.os.Looper.getMainLooper;

public class Tools {
    private Context context;
    private SharedPreferences sp;;
    private static Tools tools = new Tools();
    private PendingIntent pi;
    private AlarmManager alarm;
    private Handler mHandler;
    private NewConfig newConfig = NewConfig.getNewConfig();
    OkHttpClient client = new OkHttpClient.Builder().
            //在这里，还可以设置数据缓存等
            //设置超时时间
                    connectTimeout(5, TimeUnit.SECONDS).
                    readTimeout(20, TimeUnit.SECONDS).
                    writeTimeout(20,  TimeUnit.SECONDS).
            //错误重连
                    retryOnConnectionFailure(true).
                    build();

    public static Tools getTools() {
        return tools;
    }

    private Tools() {
        this.context = MyApplication.getInstance().getApplicationContext();
        sp = context.getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        pi = PendingIntent.getBroadcast(context, 0, new Intent("TimedTask"), 0);
        alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    //往SD卡写入文件的方法
    public void savaFileToSD(String filename, String filecontent) throws Exception {
        //如果手机已插入sd卡,且app具有读写sd卡的权限
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
            //这里就不要用openFileOutput了,那个是往手机内存中写数据的
            FileOutputStream output = new FileOutputStream(filename);
            output.write(filecontent.getBytes());
            //将String字符串以字节流的形式写入到输出流中
            output.close();
            //关闭输出流
        } else Toast.makeText(context, "文件不存在或者不可读写", Toast.LENGTH_SHORT).show();
    }

    //读取SD卡中文件的方法
    public static String readFromSD(String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //打开文件输入流
            FileInputStream input = new FileInputStream(filename);
            byte[] temp = new byte[input.available()];

            int len = 0;
            //读取文件内容:
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            //关闭输入流
            input.close();
        }
        return sb.toString();
    }

    //计算时间差
    public int getDatePoor(String configTime) {

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        Date date = null;
        //转换成Date型
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(configTime);
            // long ns = 1000;
            // 获得两个时间的毫秒时间差异
            Date dNow = new Date();
            long diff = dNow.getTime() - date.getTime();
//        // 计算差多少天
//        long day = diff / nd;
//        // 计算差多少小时
//        long hour = diff % nd / nh;
//        //计算差多少分钟
//        long min = diff % nd % nh / nm;
            // 计算差多少秒//输出结果
            // long sec = diff % nd % nh % nm / ns;
            long min = diff / nm;
            return (int) min;
        } catch (ParseException e) {
            return 0;
        }

    }

    //删除文件
    public void DeleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                }
                for (File f : childFile) {
                    DeleteFile(f);
                }
                file.delete();
            }
        }
    }

    private String parseInfo(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    //判断网络状态
    public boolean isNetworkConnected() {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //判断是否是wifi
    public boolean iswifi() {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        Boolean result = info.getType() == ConnectivityManager.TYPE_WIFI;
        MyService.beWifi = result;
        return result;
    }

    //执行脚本
    public void execShell(String cmd) {
        try {
            //权限设置
            Process p = Runtime.getRuntime().exec("su");  //开始执行shell脚本
            //获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            //将命令写入
            dataOutputStream.writeBytes(cmd);
            //提交命令
            dataOutputStream.flush();
            //关闭流操作
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
            mes("执行失败");
        }
    }

    //带输出执行
    public String execShellWithOut(String cmd) {
        try {
            DataOutputStream dos = null;
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            String data = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null
                    && !line.equals("null")) {
                data += line + "\n";
            }
            final String result = data;
            dos.close();
            in.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "失败";
        }
    }

    //卸载脚本
    public void delete() {
        File file = new File(this.context.getApplicationContext().getFilesDir().getAbsolutePath());
        try {
            DeleteFile(file);
            mes("卸载成功");
//            file.mkdir();
        } catch (Exception e) {
            mes("卸载失败");
        }

    }

    //开启脚本
    public void open() {
        try {
            execShell(context.getFilesDir() + "/start.sh");
            detection();
        } catch (Exception e) {
            e.printStackTrace();
            mes("开启失败");
        }

    }

    //关闭脚本
    public void stop() {
        try {
            execShell(context.getFilesDir() + "/stop.sh");
            detection();
        } catch (Exception e) {
            e.printStackTrace();
            mes("关闭失败");
        }
    }

    //检测脚本
    public void detection() {
        try {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            String result = "";
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!sp.getBoolean("onlyCheckIp", false))
                            {
                                result = execShellWithOut(context.getFilesDir() + "/check.sh");
                            }
                            mes(checkTiny()+checkHttpAndHttps()+checkip()+result);
                        }
                    }
            ).start();
        } catch (Exception e) {
            e.printStackTrace();
            mes("检测失败");
        }
    }

    //写入模式
    private void writeConfig(String config){
        try {
            savaFileToSD(context.getFilesDir() + "/tiny.conf", config);
        } catch (Exception e) {
            mes("写入失败");
        }
    }

    //检测tiny状态
    private String checkTiny(){
        String tiny =RxShellTool.execCmd(new String[]{"ps|grep tiny|grep -v grep", "ps|grep Tiny|grep -v grep"}, true).successMsg;
        if(tiny.length() > 0)
            return "tiny    √\n";
        else return "tiny    ×\n";
    }
    //检测ip
    private String checkip() {
        String urlHead = "http://wkhelper.vtop.design/KingCardServices/ip.php?way=";
        String url_ip = urlHead + sp.getString("ipPort", "ipip");
        String way = sp.getString("ipWay", "shell");
        if(way.equals("shell")){
            try {
                String result_curl = RxShellTool.execCmd(context.getFilesDir() + "/tools/" + "curl -H \"Accept-Encoding: gzip\" " + url_ip +" | gunzip | more", true, true).successMsg;
                if (result_curl.length() > 2) return result_curl;
                else return "ip查询失败";
            } catch (Exception e) {
                e.printStackTrace();
                return "ip查询失败";
            }
        }else if(way.equals("helper")){
            try {
                String result = run(url_ip);;
                Log.i("result", result);
                if (result.length() > 2) return result;
                else return "ip查询失败";
            } catch (Exception e) {
                e.printStackTrace();
                return "ip查询失败";
            }
        }else if(way.equals("browser")){
            Uri uri = Uri.parse("http://helper.vtop.design/KingCardServices/checkip.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return "";
        }else return "";
    }

    //检测http,https连通
    private String checkHttpAndHttps(){
        String http = executeHttpGet("http://qq.pinyin.cn/") ?  "√" : "×";
        String https = executeHttpGet("https://hao.360.cn/?src=lm&ls=n16dde9928b ") ?  "√" : "×";
        return "Http测试：" + http + "\nHttps测试：" + https +"\n";
    }

    //自动抓包
    public void autopull() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        restartTimedTask();
                        String toolPath = context.getFilesDir().getAbsolutePath() + "/tools/";
                        //检查必要文件
                        File dir = new File(toolPath);
                        if (!dir.exists()) dir.mkdir();
                        String[] necessaryFile = {"curl", "tcpdump.bin"};
                        for (String s : necessaryFile) {
                            File file = new File(toolPath + s);
                            if (!file.exists()) {
                                Log.i("tool", toolPath + s);
                                copyFile(s, toolPath);
                            }
                        }
                        //关闭脚本
                        RxShellTool.execCmd(context.getFilesDir() + "/stop.sh", true);
                        try {
                                if(sp.getString("dynamic","QQ").equals("QQ")){
                                String[] textres = null;
                                //强制抓包
                                replaceTxtByStr();
                                for (int i = 0;i <3;i++){
                                    RxShellTool.execCmd(new String[]{"pm enable com.tencent.mtt","am force-stop com.tencent.mtt","am start -n com.tencent.mtt/.MainActivity"}, true);
                                    String text = execShellWithOut(toolPath + "tcpdump.bin -i any -c " + sp.getString("Number_of_packages", "5") + " port 8090 -s 1024 -A -l");
                                    textres= getGuidToken(text);
                                    if (textres!=null && textres[1]!=newConfig.getToken()) break;
                                }
                                if (textres != null) {
                                    mes("抓取成功");
                                    try {
                                        try{
                                            MainActivity.updataUI(120, "更新\nGuid：" + textres[0] + "\nToken：" + textres[1] +"\n\n");
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        writeConfig(newConfig.generateConfig(new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format(new Date()),textres[0], textres[1]));
                                        showDialog();
                                    } catch (Exception e) {
                                        mes("未知错误");
                                    }
                                } else {
                                    mes("全是空包，请重试");
                                }
                            }else if(sp.getString("dynamic","QQ").equals("UC")){
                                String proxy = null;
                                for (int i = 0;i <3;i++){
                                    RxShellTool.execCmd(new String[]{"am force-stop com.UCMobile","am start -n com.UCMobile/com.UCMobile.main.UCMobile"}, true);
                                    String text = execShellWithOut(toolPath + "tcpdump.bin -i any -c " + sp.getString("Number_of_packages", "5") + " port 8128 -s 1024 -A -l");
                                    proxy= getProxy(text);
                                    if (proxy!=null) break;
                                }
                                if (proxy != null) {
                                    mes("抓取成功");
                                    try {
                                        try{
                                            MainActivity.updataUI(120, "更新\nProxy：" + proxy + "\n\n");
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        writeConfig(newConfig.generateUCConfig(new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format(new Date()),proxy));
                                        showDialog();
                                    } catch (Exception e) {
                                        mes("未知错误");
                                    }
                                } else {
                                    mes("全是空包，请重试");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mes("抓取失败");
                        } finally {
                            if(sp.getString("dynamic","QQ").equals("QQ")){
                                if (sp.getBoolean("iceBrowser", false)) RxShellTool.execCmd("pm disable com.tencent.mtt", true);
                                RxShellTool.execCmd("am force-stop com.tencent.mtt", true);
                            }else if(sp.getString("dynamic","QQ").equals("UC")){
                                RxShellTool.execCmd("am force-stop com.UCMobile", true);
                            }
                            open();
                        }
                    }
                }
        ).start();
    }

    //获取服务器配置
    public void getConfig() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        restartTimedTask();
                        int i = 0;
                        while (i < 2) {
                            if (isNetworkConnected()) {
                                try {
                                    String[] config = receive();
                                    if (config != null) {
                                        try {
                                            if(sp.getString("dynamic","QQ").equals("QQ")){
                                                MainActivity.updataUI(Integer.parseInt(config[0]), "更新\nGuid：" + config[1] + "\nToken：" + config[2] +"\n\n");
                                                mes("获取成功，大概剩余" + config[0] + "分钟");
                                                writeConfig(newConfig.generateConfig(config[3], config[1],config[2]));
                                            }else if(sp.getString("dynamic","QQ").equals("UC")){
                                                MainActivity.updataUI(Integer.parseInt(config[0]), "更新\nProxy：" + config[1] +"\n\n");
                                                writeConfig(newConfig.generateUCConfig(config[2], config[1]));
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        break;
                                    } else {
                                        i++;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mes("获取失败准备重试,请检查网络");
                                    i++;
                                }
                            } else {
                                i++;
                                mes("无网络连接,请打开网络");
                                RxShellTool.execCmd("svc data enable",true);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (i > 2) mes("获取失败");
                        open();
                    }
                }
        ).start();
    }

    //获取配置
    public String[] receive() {
        JSONObject con;
        int lastTime;
        try {
            String response = "";

            if(sp.getString("dynamic","QQ").equals("QQ")){
                response = run("http://helper.vtop.design/KingCardServices/get_config.php?id=1");
                con = new JSONObject(response);
                if (con != null) {
                    lastTime = (120-getDatePoor(con.getString("Time")));
                    if(lastTime>20)  return new String[]{String.valueOf(lastTime),con.getString("Guid"),con.getString("Token"),con.getString("Time")};
                    else mes("服务器最新配置已失效，请手动抓包");
                }
                response = run(sp.getString("",""));
                if (response != null) {
                    try{
                        con = new JSONObject(response);
                        if (con != null) {
                            lastTime = (120-getDatePoor(con.getString("Time")));
                            if(lastTime>20)  return new String[]{String.valueOf(lastTime),con.getString("Guid"),con.getString("Token"),con.getString("Time")};
                            else mes("服务器最新配置已失效，请手动抓包");
                        }
                    }catch (JSONException e){
                        String[] config = response.split(",");
                        if(config[1].length()>10){
                            return new String[]{String.valueOf(90),con.getString("Guid"),con.getString("Token"),con.getString("Time")};
                        }
                    }
                }
                execShell(context.getFilesDir() + "/stop.sh");
            }else if(sp.getString("dynamic","QQ").equals("UC")){
                response = run("http://helper.vtop.design/KingCardServices/uc/get_config.php?id=1");
                con = new JSONObject(response);
                if (con != null) {
                    return new String[]{String.valueOf(120),con.getString("Proxy"),con.getString("Time")};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //测试网络连通性
    private Boolean executeHttpGet(String path) {

        HttpURLConnection con = null;
        InputStream in = null;
        try {
            con = (HttpURLConnection) new URL(path).openConnection();
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                in = con.getInputStream();
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    //获取guid和token
    public String[] getGuidToken(String text) {
        String pattern = "Q-GUID[: |]*(\\w+?)[,\\s]";
        String pattern2 = "Q-Token[: |]*(\\w+?)[\\s,]";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        Pattern r2 = Pattern.compile(pattern2);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(text);
        Matcher m2 = r2.matcher(text);
        if (m.find() && m2.find()) {
            String guid = m.group(1);
            String token = m2.group(1);
            String[] txt = new String[2];
            txt[0] = guid;
            txt[1] = token;
            return txt;
        } else return null;

    }
    //获取Proxy
    public String getProxy(String text) {
        String pattern = "(Proxy-Authorization:.*=?)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(text);
        if (m.find()) {
            String proxy = m.group(1);
            return proxy;
        } else return null;

    }

    //上传配置
    public void showDialog() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            String path = "http://" + context.getString(R.string.host) + "/KingCardServices/create_config.php?id=1";
                            jsonObject.put("Time", newConfig.getTime());
                            if(sp.getString("dynamic","QQ").equals("QQ")){
                                jsonObject.put("Guid", newConfig.getGuid());
                                jsonObject.put("Token", newConfig.getToken());
                            }else if(sp.getString("dynamic","QQ").equals("UC")){
                                jsonObject.put("Proxy", newConfig.getProxy());
                                path = "http://" + context.getString(R.string.host) + "/KingCardServices/uc/create_config.php?id=1";
                            }
                            HttpURLConnection con = null;
                            URL url = new URL(path);
                            con = (HttpURLConnection) url.openConnection();
                            con.setDoInput(true);
                            con.setDoOutput(true);
                            con.setUseCaches(false);
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Connection", "keep-alive");
                            con.setRequestProperty("contentType", "application/json");

                            con.connect();

                            OutputStream out = con.getOutputStream();
                            // 写入请求的字符串
                            System.out.println(jsonObject.toString());
                            out.write((jsonObject.toString()).getBytes("utf-8"));
                            out.flush();
                            out.close();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String lines;
                            StringBuffer sbf = new StringBuffer();
                            while ((lines = reader.readLine()) != null) {
                                lines = new String(lines.getBytes(), "utf-8");
                                sbf.append(lines);
                            }
                            System.out.println("返回来的报文：" + sbf.toString());
                        } catch (Exception e) {
                            mes("上传失败");
                        }
                        ;
                    }
                }
        ).start();

    }

    //发送消息
    public void mes(final String text) {
        if(text.length()<20){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new XToast(MyApplication.getInstance()) // 传入 Application 对象表示设置成全局的
                            .setDuration(2000)
                            .setView(ToastUtils.getToast().getView())
                            .setAnimStyle(android.R.style.Animation_Translucent)
                            .setText(android.R.id.message, text)
                            .show();
                }
            });
        }else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new XToast(MyApplication.getInstance()) // 传入 Application 对象表示设置成全局的
                            .setDuration(3000)
                            .setView(R.layout.toast_hint)
                            .setAnimStyle(android.R.style.Animation_Translucent)
                            .setText(android.R.id.message, text)
                            .show();
                }
            });
        }
    }

    //复制
    public void copy(CharSequence text) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    //唤醒APP
    public void openApp(String name) {
        /**获取ActivityManager*/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        /**获得当前运行的task(任务)*/
        Boolean hasFind = false;
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            /**找到本应用的 task，并将它切换到前台*/
            if (taskInfo.topActivity.getPackageName().equals(name)) {
                hasFind = true;
                activityManager.moveTaskToFront(taskInfo.id, 0);
                break;
            }
        }
        if (!hasFind) {
            // 获取包管理器
            PackageManager manager = context.getPackageManager();
            // 指定入口,启动类型,包名
            Intent intent = new Intent(Intent.ACTION_MAIN);//入口Main
            intent.addCategory(Intent.CATEGORY_LAUNCHER);// 启动LAUNCHER,跟MainActivity里面的配置类似
            intent.setPackage(name);//包名
            //查询要启动的Activity
            List<ResolveInfo> apps = manager.queryIntentActivities(intent, 0);
            if (apps.size() > 0) {//如果包名存在
                ResolveInfo ri = apps.get(0);
                // //获取包名
                String packageName = ri.activityInfo.packageName;
                //获取app启动类型
                String className = ri.activityInfo.name;
                //组装包名和类名
                ComponentName cn = new ComponentName(packageName, className);
                //设置给Intent
                intent.setComponent(cn);
                //根据包名类型打开Activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                mes("未安装应用");
            }
        }
    }

    //收起通知栏
    public void collapseStatusBar() {
        try {
            @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    //解压脚本包
    public void unzip(String srcFile, String desFile) {
        try {
            ZipExtractorTask task = new ZipExtractorTask(srcFile, desFile, context, true);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
            mes("解压失败");
        }
    }

    //从assets安装工具
    public void copyFile(String filename, String path) {
        InputStream in = null;
        FileOutputStream out = null;
        File file = new File(path + filename);
        if (!file.exists()) {
            try {
                in = this.context.getAssets().open(filename); // 从assets目录下复制
                out = new FileOutputStream(file);
                int length = -1;
                byte[] buf = new byte[in.available()];
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        execShell("chmod -R 777 " + path + filename);
    }

    //获取uri真实路径
    public String getRealPathFromUri(Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    //查找指定文件
    public List<String> GetFiles(String Path, String Extension, boolean IsIterative)  //搜索目录，扩展名，是否进入子文件夹
    {
        List<String> lstFile = new ArrayList<String>();  //结果 List
        File[] files = new File(Path).listFiles();

        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isFile()) {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))  //判断扩展名
                    lstFile.add(f.getPath());

            } else if (IsIterative && f.isDirectory()) {
                GetFiles(f.getPath(), Extension, IsIterative);
            }
        }
        return lstFile;
    }

    //启动定时任务
    public void openTimedTask() {
        int anHour = sp.getInt("autotime", 30) * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    //关闭定时任务
    public void closeTimedTask() {
        try {
            alarm.cancel(pi);
        } catch (Exception e) {
            e.printStackTrace();
            mes("停止定时任务失败");
        }
        MyService.needDo = false;
    }

    //重置定时任务
    public void restartTimedTask() {
        closeTimedTask();
        openTimedTask();
    }

    //多任务列表隐藏
    public void hideInRecents() {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (am != null && android.os.Build.VERSION.SDK_INT >= 21) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                ((ActivityManager.AppTask) tasks.get(0)).setExcludeFromRecents(sp.getBoolean("hide", false));
            }
        }
    }

    //修改指定内容
    public void replaceTxtByStr() {
        String temp = "";
        String oldStr = "    <int name=\"key_sim_order_status\" value=\"0\" />";
        String replaceStr = "    <int name=\"key_sim_order_status\" value=\"2\" />\n";
        String inPath = "/data/data/com.tencent.mtt/shared_prefs/public_settings.xml";
        String outPath = context.getFilesDir() + "/public_settings.xml";
        try {
            RxShellTool.execCmd(new String[]{"chmod -R 777 " + inPath, "cp -f " + inPath + " " + context.getFilesDir(), "chmod -R 777 " + outPath}, true);
            File file = new File(outPath);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            // 保存该行前面的内容
            for (int j = 1; (temp = br.readLine()) != null
                    && !temp.equals(oldStr); j++) {
                buf = buf.append(temp + "\n");
            }
            // 将内容插入
            buf = buf.append(replaceStr);
            // 保存该行后面的内容
            while ((temp = br.readLine()) != null) {
                buf = buf.append(temp);
            }
            br.close();
            File fileout = new File(outPath);
            FileOutputStream fos = new FileOutputStream(fileout);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
            RxShellTool.execCmd("cp -f " + outPath + " " + inPath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查通知栏权限有没有开启
     * 参考 SupportCompat 包中的方法： NotificationManagerCompat.from(context).areNotificationsEnabled();
     */
    public boolean isNotificationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).areNotificationsEnabled();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;

            try {
                Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
                int value = (Integer) opPostNotificationValue.get(Integer.class);
                return ((int) checkOpNoThrowMethod.invoke(appOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                    | InvocationTargetException | IllegalAccessException | RuntimeException ignored) {
                return true;
            }
        } else {
            return true;
        }
    }
    //通知权限跳转
    public void isHasNotifications(){
        boolean isOpened = NotificationManagerCompat.from(context).areNotificationsEnabled();
        if(!isOpened){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new XToast(MyApplication.getInstance()) // 传入 Application 对象表示设置成全局的
                            .setDuration(6000)
                            .setView(ToastUtils.getToast().getView())
                            .setAnimStyle(android.R.style.Animation_Translucent)
                            .setText(android.R.id.message, "开启通知权限，当然不开启并不会影响自动获取服务")
                            .show();
                }
            });
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", MyApplication.getInstance().getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }
    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
