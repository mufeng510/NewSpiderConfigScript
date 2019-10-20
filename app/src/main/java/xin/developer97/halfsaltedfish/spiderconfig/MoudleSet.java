package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

public class MoudleSet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_moudle_set);
        EditText set_moudle = (EditText)findViewById(R.id.set_moudle);
        SharedPreferences sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String QQconfig = "listen_port=65080;\n" +
                "worker_proc=1;\n" +
                "daemon=on;\n" +
                "uid=3004;\n" +
                "mode=wap;\n" +
                "\n" +
                "http_ip=替换IP;\n" +
                "http_port=8090;\n" +
                "http_del=\"Host,X-Online-Host\";\n" +
                "http_first=\"[M] http://[H][U] [V]\\r\\n\n" +
                "Q-GUID:替换GUID\\r\\n\n" +
                "Q-Token:替换Token\n" +
                "\n" +
                "\\r\\n\n" +
                "Host: [H]\\r\\n" +
                "Connection: Keep-Alive\\r\\n\";\n" +
                "\n" +
                "https_connect=on;\n" +
                "https_ip=替换IP;\n" +
                "https_port=8091;\n" +
                "https_del=\"Host,X-Online-Host\";\n" +
                "https_first=\"[M] [H] [V]\\r\\n\n" +
                "Host: [H]\\r\\n" +
                "Proxy-Connection: keep-alive\\r\\n\n" +
                "Q-GUID:替换GUID\\r\\n\n" +
                "Q-Token:替换Token\n" +
                "\\r\\n\";\n" +
                "\n" +
                "dns_tcp=http;\n" +
                "dns_listen_port=65053;\n" +
                "dns_url=\"119.29.29.29\";\n";
        String UCconfig = "listen_port=65080;\n" +
                "worker_proc=0;\n" +
                "uid=3004;\n" +
                "mode=wap;\n" +
                "daemon=on;\n" +
                "\n" +
                "http_ip=101.71.140.5;\n" +
                "http_port=8128;\n" +
                "http_del=\"Host,X-Online-Host\"; \n" +
                "http_first=\"[M] http://[H][U] [V]\\r\\n代码\\r\\n\";\n" +
                "\n" +
                "https_connect=on;\n" +
                "https_ip=101.71.140.5;\n" +
                "https_port=8128;\n" +
                "https_del=\"Host,X-Online-Host\";\n" +
                "https_first=\"[M] [H]/ [V]\\r\\n代码\\r\\n\";\n" +
                "\n" +
                "dns_tcp=http;\n" +
                "dns_listen_port=65053;\n" +
                "dns_url=\"119.29.29.29\";";
        set_moudle.setText(sp.getString("module",QQconfig.replace("替换IP",sp.getString("ip","157.255.173.182"))));

        Button qqMoudle = (Button)findViewById(R.id.qqMoudle);
        qqMoudle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("dynamic","QQ");
                editor.putBoolean("openTask",true);
                set_moudle.setText(QQconfig);
            }
        });
        Button ucMoudle = (Button)findViewById(R.id.ucMoudle);
        ucMoudle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("dynamic","UC");
                editor.putBoolean("openTask",false);
                set_moudle.setText(UCconfig);
            }
        });
        Button setIp = (Button)findViewById(R.id.setIp);
        setIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MoudleSet.this,setIp);
                popup.getMenuInflater().inflate(R.menu.menu_ip, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        editor.putString("ip",item.getTitle().toString());
                        set_moudle.setText(QQconfig.replace("替换IP",item.getTitle()));
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button moudle_save = (Button)findViewById(R.id.moudle_save);
        moudle_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("module",set_moudle.getText().toString());
                editor.commit();
                finish();
            }
        });
    }
}
