package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.*;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.util.Log;
import android.widget.RemoteViews;

public class MyService extends Service {
    private static String path ="";
    public static Boolean needDo = false, beWifi = false, notFirstRun = false, isScreenOn =true;
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo netInfo;
    private Tools tools = Tools.getTools();
    SharedPreferences sp;
    private NotificationManager notificationManager;
    private String notificationId = "channelId";
    private String notificationName = "channelName";
    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    //通知栏按钮点击事件对应的ACTION（标识广播）
    public final static String ACTION_BUTTON = "xianyu.intent.action.ButtonClick";
    public final static int BTN_1 = 1, BTN_2 = 2, BTN_3 = 3, BTN_4 = 4, BTN_5 = 5;
    public static RemoteViews rv;


    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //通知
    private Notification getNotification() {
        rv = new RemoteViews(getPackageName(), R.layout.notice);

        //点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        /* 图标按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BTN_1);
        PendingIntent intent_jump = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btn1, intent_jump);
        /* 获取配置按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BTN_2);
        PendingIntent intent_get = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btn2, intent_get);
        /* 关闭脚本按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BTN_3);
        PendingIntent intent_close = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btn3, intent_close);
        /* 自动抓包 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BTN_4);
        PendingIntent intent_pull = PendingIntent.getBroadcast(this, 4, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btn4, intent_pull);
        /* 检测位置 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BTN_5);
        PendingIntent intent_ip = PendingIntent.getBroadcast(this, 5, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.btn5, intent_ip);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.wangka)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContent(rv);
        //设置Notification的ChannelID,否则不能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyService","创建一次服务");
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        path = getApplicationContext().getFilesDir() + "/tiny.conf";
        //通知栏
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(614, getNotification());
        //监听屏幕
        IntentFilter mScreenFilter = new IntentFilter();
        mScreenFilter.addAction("android.intent.action.USER_PRESENT");
        mScreenFilter.addAction("android.intent.action.SCREEN_OFF");
        MyService.this.registerReceiver(mScreenReceiver, mScreenFilter);
        //监听网络变化
        IntentFilter myNetworkFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        MyService.this.registerReceiver(MyNetworkReceiver, myNetworkFilter);
        //监听通知栏
        IntentFilter intentFilter = new IntentFilter(ACTION_BUTTON);
        MyService.this.registerReceiver(receiver, intentFilter);
        //定时任务
        IntentFilter timedTask = new IntentFilter("TimedTask");
        MyService.this.registerReceiver(AutoUpdateReceiver, timedTask);
    }

    /**
     * 采用AlarmManager机制执行定时任务
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService","开始一遍服务");
        if (sp.getBoolean("openTask", true)) {
            if (notFirstRun) {
                try {
                    if (!tools.iswifi()) {
                        //这是定时所执行的任务
                        switch (sp.getString("autoWay", "getconfig")) {
                            case "getconfig":
                                //唤醒
                                if (sp.getBoolean("screenOff", false)) {
                                    if (isScreenOn) needDo = true;
                                    else tools.getConfig();
                                }else{
                                    tools.getConfig();
                                }
                                break;
                            case "catch":
                                if (sp.getBoolean("screenOff",false)){
                                    if(isScreenOn){
                                        needDo = true;
                                    } else tools.autopull();
                                }else tools.autopull();
                                break;
                        }
                    }
                } catch (Exception e) {
                    tools.mes("自动任务执行出错");
                }
            }
            tools.openTimedTask();
            notFirstRun = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        MyService.this.unregisterReceiver(mScreenReceiver);
        MyService.this.unregisterReceiver(MyNetworkReceiver);
        MyService.this.unregisterReceiver(receiver);
        MyService.this.unregisterReceiver(AutoUpdateReceiver);
    }

    //定时任务
    public BroadcastReceiver AutoUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, MyService.class));
            } else {
                context.startService(new Intent(context, MyService.class));
            }
        }
    };
    //屏幕状态
    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.USER_PRESENT")) {
                Log.i("MyService","解锁");
                isScreenOn = true;
                if (sp.getBoolean("autoCheckAfterScreenOn", true)) {
                    if (!tools.iswifi()) tools.detection();
                }
            }
            if (action.equals("android.intent.action.SCREEN_OFF")){
                Log.i("MyService","灭屏");
                isScreenOn = false;
                if (!tools.iswifi() && needDo) {
                    switch (sp.getString("autoWay", "getconfig")){
                        case "getconfig":
                            tools.getConfig();
                            break;
                        case "catch":
                            tools.autopull();
                            break;
                    }
                }
            }
        }

    };
    //网络状态
    private BroadcastReceiver MyNetworkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            netInfo = mConnectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                /////WiFi网络
                if (sp.getBoolean("changeOpen",false)) {
                    if (!beWifi){
                        beWifi = true;
                        tools.stop();
                    }
                }
                beWifi = true;
            } else {
                ////////网络断开
                if (beWifi) {
                    beWifi = false;
                    if (sp.getBoolean("changeOpen",false)) tools.getConfig();
                }
            }

        }
    };

    /**
     * （通知栏中的点击事件是通过广播来通知的，所以在需要处理点击事件的地方注册广播即可）
     * 广播监听按钮点击事件
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_BUTTON)) {
                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case BTN_1:
                        tools.collapseStatusBar();
                        tools.openApp(getPackageName());
                        break;
                    case BTN_2:
                        tools.collapseStatusBar();
                        tools.getConfig();
                        break;
                    case BTN_3:
                        tools.collapseStatusBar();
                        tools.closeTimedTask();
                        tools.stop();
                        break;
                    case BTN_4:
                        tools.collapseStatusBar();
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        tools.autopull();
                                    }
                                }
                        ).start();
                        break;
                    case BTN_5:
                        tools.collapseStatusBar();
                        tools.detection();
                        break;
                    default:
                        break;
                }
            }
        }
    };
}
