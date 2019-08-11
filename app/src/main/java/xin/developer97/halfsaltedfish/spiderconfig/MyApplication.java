package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.Application;

import com.vondear.rxtool.RxTool;

public class MyApplication extends Application {
    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        RxTool.init(this);
    }

    public static MyApplication getInstance(){
        return myApplication;
    }
}
