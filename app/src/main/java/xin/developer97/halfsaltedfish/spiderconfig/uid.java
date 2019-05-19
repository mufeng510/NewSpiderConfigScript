package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class uid extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ListView uidView = new ListView(this);
        List<String> uidList = new ArrayList<String>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES| PackageManager.GET_PERMISSIONS);
        for (PackageInfo info : packinfos) {
            String[] premissions = info.requestedPermissions;
            if (premissions != null && premissions.length > 0) {
                for (String premission : premissions) {
                    if ("android.permission.INTERNET".equals(premission)) {
                        // System.out.println(info.packageName+"访问网络");
                        int uid = info.applicationInfo.uid;
                        if(uid>1000){
                            String name = info.applicationInfo.loadLabel(getPackageManager()).toString();
                            uidList.add(name+"\t"+uid);
                        }
                    }
                }
            }
        }
        Collections.sort(uidList);
        Collections.sort(uidList,new PinyinComparator());
        uidView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,uidList));
        setContentView(uidView);

    }
}
