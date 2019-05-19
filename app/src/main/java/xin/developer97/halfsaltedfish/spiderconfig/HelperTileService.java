package xin.developer97.halfsaltedfish.spiderconfig;


import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class HelperTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
