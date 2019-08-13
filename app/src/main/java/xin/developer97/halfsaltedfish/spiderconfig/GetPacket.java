package xin.developer97.halfsaltedfish.spiderconfig;


import android.content.*;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class GetPacket extends AppCompatActivity implements  android.view.GestureDetector.OnGestureListener{
    CharSequence old;
    Tools tools = Tools.getTools();
    SharedPreferences sp;
    GestureDetector gd;
    private static TextView information;
    private static Handler mHandler_packet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_get_packet);

        mHandler_packet = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        information = (TextView) findViewById(R.id.information);
        Button aotuget = (Button) findViewById(R.id.aotuget);
        Button copyConfig = (Button) findViewById(R.id.copyConfig);
        old = information.getText();
        final String path = getApplicationContext().getFilesDir() + "/tiny.conf";
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);

        //创建手势检测器
        gd = new GestureDetector(this,this);
        //自定义壁纸
        RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.layoutget);
        if (sp.getString("backpath", null) != null) {
            String uri = sp.getString("backpath", null);
            Drawable drawable = (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        } else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }
        //自动抓包
        aotuget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.autopull(true);
            }
        });
        //解冻QQ浏览器
        Button thawBrowser = (Button)findViewById(R.id.thawBrowser);
        thawBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.execShell("pm enable com.tencent.mtt\nam start -n com.tencent.mtt/.MainActivity");
            }
        });
        //复制
        copyConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence config = information.getText();
                if (config != old) {
                    //获取剪贴板管理器：
                    tools.copy(config);
                    Toast.makeText(GetPacket.this, "复制成功", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(GetPacket.this, "请先获取配置", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //更新ui
    public static void updataUI(final String config){
        mHandler_packet.post(new Runnable() {
            @Override
            public void run() {
                information.setText(config);
            }
        });
    }
    //写入
    public void writeOpen(String path, CharSequence config) {
        try {
            tools.savaFileToSD(path, config.toString());
            tools.mes("写入成功");
            tools.open();
        } catch (Exception e) {
            tools.mes("写入失败");
        }

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
//        float beginY = e1.getY();
//        float endY = e2.getY();

        if(beginX-endX>minMove&&Math.abs(velocityX)>minVelocity){   //左滑
//            Toast.makeText(this,velocityX+"左滑",Toast.LENGTH_SHORT).show();
        }else if(endX-beginX>minMove&&Math.abs(velocityX)>minVelocity){   //右滑
            this.finish();
//            Toast.makeText(this,velocityX+"右滑",Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}
