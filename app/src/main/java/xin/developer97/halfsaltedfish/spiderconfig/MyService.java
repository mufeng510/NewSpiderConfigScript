package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.*;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.util.Log;
import android.widget.RemoteViews;

public class MyService extends Service {
    public static Boolean hasGet = false, beWifi = false, notFirstRun = false;
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo netInfo;
    private Tools tools = new Tools();
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
        tools.setContext(getApplicationContext());
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
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
        mScreenFilter.addAction("android.intent.action.SCREEN_ON");
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

        if (sp.getBoolean("openTask", true)) {
            if (notFirstRun) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                            boolean isScreenOn = pm.isScreenOn();
                            String path = getApplicationContext().getFilesDir() + "/tiny.conf";
                            //这是定时所执行的任务
                            switch (sp.getString("autoWay", "getconfig")) {
                                case "getconfig":
                                    CharSequence config = tools.receive().getConfig();
                                    tools.savaFileToSD(path, config.toString());
                                    hasGet = true;
                                    tools.mes("已写入最新配置");
                                    //唤醒
                                    if (!tools.iswifi()) {
                                        if (!sp.getBoolean("screenOff", false)) {
                                            new Thread(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            tools.open();
                                                        }
                                                    }
                                            ).start();
                                        }
                                    }
                                    break;
                                case "catch":
                                    NewConfig newConfig = tools.autopull();
                                    if (newConfig != null) {
                                        String config2 = newConfig.getConfig();
                                        tools.savaFileToSD(path, config2);
                                        tools.showDialog(newConfig);
                                        new Thread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        tools.open();
                                                    }
                                                }
                                        ).start();
                                    }
                                    break;
                            }

                        } catch (Exception e) {
                            tools.mes("获取最新配置失败");
                        }
                    }
                }).start();
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
            if (action.equals("android.intent.action.SCREEN_ON")) {
                Log.i("MyService","亮屏");
                if (sp.getBoolean("autoCheckAfterScreenOn", true)) {
                    if (!beWifi) tools.detection();
                }
            }
            if (action.equals("android.intent.action.SCREEN_OFF")){
                Log.i("MyService","灭屏");
                if (!tools.iswifi()) {
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (hasGet) {
                                        hasGet = false;
                                        tools.open();
                                    }
                                }
                            }
                    ).start();
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
                beWifi = true;
                if (sp.getBoolean("changeOpen",false)) tools.stop();
            } else {
                ////////网络断开
                if (beWifi) {
                    beWifi = false;
                    if (sp.getBoolean("changeOpen",false)) tools.open();
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
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        NewConfig newConfig = tools.getConfig();
                                        if (newConfig != null) {
                                            String time = newConfig.getTime();
                                            final String config = newConfig.getConfig();
                                            int usetime = tools.getDatePoor(time);
                                            if ((120 - usetime) > 0) {
                                                tools.restartTimedTask();
                                                tools.mes("获取成功，大概剩余" + (120 - usetime) + "分钟");
                                                //写入
                                                String path = getApplicationContext().getFilesDir() + "/tiny.conf";
                                                try {
                                                    tools.savaFileToSD(path, config);
                                                } catch (Exception e) {
                                                    tools.mes("写入失败");
                                                }
                                                tools.open();
                                            } else tools.mes("服务器最新配置已失效，请手动抓包");
                                        } else
                                            tools.mes("获取失败");
                                    }
                                }
                        ).start();
                        break;
                    case BTN_3:
                        tools.collapseStatusBar();
                        tools.closeTimedTask();
                        tools.stop();
                        break;
                    case BTN_4:
                        tools.collapseStatusBar();
                        tools.autopull();
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
