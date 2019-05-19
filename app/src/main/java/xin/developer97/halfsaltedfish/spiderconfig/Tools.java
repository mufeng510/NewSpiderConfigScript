package xin.developer97.halfsaltedfish.spiderconfig;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.Process;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;

public class Tools {
    private static Context context;
    private SharedPreferences sp;
    public static String ip = "ip查询失败";

    public void setContext(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
    }

    public Tools() {
    }

    public Tools(Context Context) {
        super();
        this.context = Context;
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
    public String readFromSD(String filename) throws IOException {
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
        if (file.exists() == false) {
            mes("文件或文件夹不存在");
        } else {
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
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = execShellWithOut(context.getFilesDir() + "/start.sh");
                            MyService.hasGet = false;
                            if (sp.getBoolean("autoDetection", true)) {
//                                Thread.sleep(5000);
//                                String result = execShellWithOut(context.getFilesDir() + "/check.sh");
                                checkip();
                                longMes(ip + result);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mes("脚本开启失败");
                        }
                    }
                }
        ).start();
    }

    //关闭脚本
    public void stop() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = execShellWithOut(context.getFilesDir() + "/stop.sh");
                            mes("脚本关闭成功");
                            if (sp.getBoolean("autoDetection", true)) {
//                                Thread.sleep(5000);
//                                String result = execShellWithOut(context.getFilesDir() + "/check.sh");
                                checkip();
                                longMes(ip + result);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mes("脚本关闭失败");
                        }
                    }
                }
        ).start();
    }

    //检测脚本
    public void detection() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = execShellWithOut(context.getFilesDir() + "/check.sh");
                            checkip();
                            longMes(ip + result);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mes("检测失败");
                        }
                    }
                }
        ).start();
    }

    //检测ip
    private void checkip() {
        ip = "ip查询失败";
        String url_ip = sp.getString("ipPort", "http://myip.ipip.net");
        switch (sp.getString("ipWay", "shell")) {
            case "shell":
                try {
                    String result_curl = execShellWithOut(context.getFilesDir() + "/tools/" + "curl " + url_ip);
                    if (result_curl.length() > 5) {
                        switch (url_ip) {
                            case "https://ip.cn/index.php":
                                String pattern = "所在地理位置：<code>(.+?)</code>";
                                Pattern r = Pattern.compile(pattern);
                                Matcher m = r.matcher(result_curl);
                                if (m.find()) {
                                    ip = m.group(1);
                                }
                                break;
                            case "http://myip.ipip.net":
                                ip = result_curl.substring(2, result_curl.length()-4);
                                break;
                            default:
                                ip = result_curl;
                                break;
                        }
                    }
                    Log.i("ip", ip);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "helper":
                try {
                    String result = executeHttpGet(url_ip);
                    Log.i("url_ip", url_ip);
                    Log.i("result", result);
                    if (result.length() > 2) {
                        switch (url_ip) {
                            case "http://myip.ipip.net":
                                ip = result;
                                break;
                            case "http://cip.cc":
                                String pattern = "<pre>([\\S\\s]+?)URL";
                                Pattern r = Pattern.compile(pattern);
                                Matcher m = r.matcher(result);
                                if (m.find()) {
                                    ip = m.group(1);
                                }
                                break;
                            case "https://ip.cn/index.php":
                                String pattern2 = "所在地理位置：<code>(.+?)</code>";
                                Pattern r2 = Pattern.compile(pattern2);
                                Matcher m2 = r2.matcher(result);
                                if (m2.find()) {
                                    ip = m2.group(1);
                                }
                                break;
                        }
                    }
                    Log.i("ip", ip);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "browser":
                Uri uri = Uri.parse(url_ip);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
                break;
            case "not":
                break;
        }
    }

    //自动抓包
    public NewConfig autopull() {
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                }
//        ).start();
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.tencent.mtt");
//                            if (intent != null) {
//                                intent.putExtra("type", "110");
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.setAction("android.intent.action.VIEW");
//                                Uri uri = Uri.parse("https://www.cnblogs.com/halfsaltedfish/articles/10569677.html");
//                                intent.setData(uri);
//                                context.startActivity(intent);
//                            } else mes("打开失败：未安装应用");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            mes("无法打开");
//                        }
//                    }
//                }
//        ).start();
        try {
            if(sp.getBoolean("iceBrowser",true)){
                execShellWithOut(context.getFilesDir() + "/stop.sh\n"+"pm enable com.tencent.mtt\n"+context.getFilesDir() + "/tools/" +"am start -n com.tencent.mtt/.MainActivity -d http://qbact.html5.qq.com/newtickets?addressbar=hide&sdi_from=44");
            }
            else {
                execShellWithOut(context.getFilesDir() + "/stop.sh\n"+context.getFilesDir() + "/tools/" +"am start -n com.tencent.mtt/.MainActivity -d http://qbact.html5.qq.com/newtickets?addressbar=hide&sdi_from=44");
                longMes("无任何提示请打开更多网页或检查免流通道状态");
            }
            String text = execShellWithOut(context.getFilesDir() + "/tools/" + "tcpdump.bin -i any -c 5 port 8090 -s 1024 -A -l");
            Log.i("pkg", text);
            String[] textres = getGuidToken(text);
            Log.i("pgk+",textres[1]);
            if (textres != null) {
                mes("抓取成功");
                NewConfig newConfig = new NewConfig(context, textres[0], textres[1]);
                if (newConfig != null) {
                    try {
                        restartTimedTask();
                        String path = context.getFilesDir() + "/tiny.conf";
                        try {
                            savaFileToSD(path, newConfig.getConfig());
                            showDialog(newConfig);
                        } catch (Exception e) {
                            mes("写入失败");
                        }
                    } catch (Exception e) {
                        mes("未知错误");
                    }
                }
                return newConfig;
            } else {
                longMes("未抓取到可用信息，请重试");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            mes("抓取失败");
            return null;
        } finally {
            if(sp.getBoolean("iceBrowser",true))
            execShell(context.getFilesDir() + "/tools/" + "am force-stop com.tencent.mtt\npm disable-user com.tencent.mtt");
            else
            execShell(context.getFilesDir() + "/tools/" + "am force-stop com.tencent.mtt");
            open();
        }
    }

    //获取服务器配置
    public NewConfig getConfig() {
        execShellWithOut(context.getFilesDir() + "/stop.sh");
        if (isNetworkConnected()) {
            try {
                NewConfig newConfig = receive();
                return newConfig;
            } catch (Exception e) {
                e.printStackTrace();
                mes("获取失败,请检查网络");
                return null;
            }
        } else {
            mes("请检查网络连接");
            return null;
        }

    }

    //获取配置
    public NewConfig receive() {
        String response = executeHttpGet("http://" + context.getString(R.string.host) + "/android_connect/get_config.php");
        if (response != "{\"success\":0,\"message\":\"No products found\"}") {
            try {
                JSONObject res = new JSONObject(response);
                JSONArray config = new JSONArray(res.getString("configs"));
                JSONObject con = config.getJSONObject(0);
                NewConfig newConfig = new NewConfig(context, con.getString("Time"), con.getString("Guid"), con.getString("Token"));
                return newConfig;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    //访问网络
    private String executeHttpGet(String path) {

        HttpURLConnection con = null;
        InputStream in = null;
        try {
            con = (HttpURLConnection) new URL(path).openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {

                in = con.getInputStream();
                return parseInfo(in);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

//    public String executeHttpGet(String path)    {
//        try {
//            URL url = new URL(path);
//            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//            conn.setConnectTimeout(2000);
//            conn.setReadTimeout(2000);
//            InputStream inStream =  conn.getInputStream();
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            byte[]  buffer = new byte[1204];
//            int len = 0;
//            while ((len = inStream.read(buffer)) != -1)
//            {
//                outStream.write(buffer,0,len);
//            }
//            inStream.close();
//            byte[] data = outStream.toByteArray();
//            String html = new String(data);
//            return html;
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//
//    }
//    private String executeHttpGet_GBK (String path) {
//        try {
//            URL url = new URL(path);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.setReadTimeout(3000);
//            connection.setConnectTimeout(3000);
//            if (connection.getResponseCode() == 200) {
//                InputStream inputStream = connection.getInputStream();
//                Addbean addbean = gson.fromJson(new InputStreamReader(inputStream,"gbk"), Addbean.class);
//                List<Addbean.ContentsBean> contents = addbean.getContents();
//                return contents;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        return null;
//    }
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

    //上传配置
    public void showDialog(final NewConfig newConfig) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        String time = newConfig.getTime();
                        String guid = newConfig.encryptionGuid();
                        String token = newConfig.encryptionToken();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("Time", time);
                            jsonObject.put("Guid", guid);
                            jsonObject.put("Token", token);
                            HttpURLConnection con = null;
                            String path = "http://" + context.getString(R.string.host) + "/android_connect/create_config.php";
                            try {
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
//                                System.out.println("返回来的报文："+sbf.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            mes("上传失败");
                        }
                        ;
                    }
                }
        ).start();

    }

    //发送消息
    public void mes(final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //发送长消息
    public void longMes(final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
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

    public boolean isRunningForeground() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        /**枚举进程*/
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
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
    public static String getRealPathFromUri(Uri uri) {
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
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent("TimedTask"), 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        int anHour = sp.getInt("autotime", 30) * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    //关闭定时任务
    public void closeTimedTask() {
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent("TimedTask"), 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        try {
            alarm.cancel(pi);
        } catch (Exception e) {
            e.printStackTrace();
            mes("停止定时任务失败");
        }
        MyService.hasGet = false;
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

}
