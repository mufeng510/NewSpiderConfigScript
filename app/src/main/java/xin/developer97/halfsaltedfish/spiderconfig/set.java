package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class set extends AppCompatActivity {
    private SharedPreferences sp;
    private String backpath;
    EditText autotime;
    Switch hide, autoCheckAfterScreenOn, screenOff, changeOpen, openTask,iceBrowser,onlyCheckIp;
    Tools tools = Tools.getTools();
    Button ipPorts,ipway;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            backpath = tools.getRealPathFromUri(uri);
            tools.mes("ok，保存后重启生效");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set);
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutset);
        if (sp.getString("backpath", null) != null) {
            String uri = sp.getString("backpath", null);
            Drawable drawable = (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        } else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }

        onlyCheckIp = (Switch)findViewById(R.id.onlyCheckIp);
        hide = (Switch)findViewById(R.id.hide);
        iceBrowser = (Switch)findViewById(R.id.iceBrowser);
        autoCheckAfterScreenOn = (Switch) findViewById(R.id.autoCheckAfterScreenOn);
        screenOff = (Switch) findViewById(R.id.screenOff);
        changeOpen = (Switch) findViewById(R.id.changeOpen);
        openTask = (Switch) findViewById(R.id.openTask);
        RadioGroup autoWays = (RadioGroup) findViewById(R.id.autoWays);
        ipPorts = (Button) findViewById(R.id.ipPorts);

        autotime = (EditText) findViewById(R.id.autotime);

        Button background = (Button) findViewById(R.id.background);
        ipway = (Button)findViewById(R.id.ipWay);
        Button setting = (Button) findViewById(R.id.setting);

        //关于
        final TextView About_software = findViewById(R.id.About_software);
        //获取本地版本号
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "获取失败";
        }
        final String final_versionName = versionName;
        About_software.setText("当前版本："+ final_versionName+"\n"+"最新版本：" + MainActivity.versionName_new);
        //开启设置以保存的设置
        settingStart();

        //自定义壁纸
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, 1100);
            }
        });
        //ip查询方式
        ipway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(set.this,ipway);
                popup.getMenuInflater().inflate(R.menu.menu_pop, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.check_ip_by_shell:
                                ipway.setText("终端");
                                editor.putString("ipWay","shell");
                                break;
                            case R.id.check_ip_by_helper:
                                ipway.setText("助手");
                                editor.putString("ipWay","helper");
                                break;
                            case R.id.check_ip_by_browser:
                                ipway.setText("浏览器");
                                editor.putString("ipWay","browser");
                                break;
                            case R.id.not_check_ip:
                                ipway.setText("不查询");
                                editor.putString("ipWay","not");
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        //ip查询接口
        ipPorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(set.this,ipPorts);
                popup.getMenuInflater().inflate(R.menu.menu_ipapi, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.check_ip_use_ipip:
                                ipPorts.setText("ipip");
                                editor.putString("ipPort","ipip");
                                break;
                            case R.id.check_ip_use_cip:
                                ipPorts.setText("cip");
                                editor.putString("ipPort","cip");
                                break;
                            case R.id.check_use_ipcn:
                                ipPorts.setText("纯真");
                                editor.putString("ipPort","cz88");
                                break;
                            case R.id.check_use_pconline:
                                ipPorts.setText("pconline");
                                editor.putString("ipPort","pconline");
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        //自动任务方式
        autoWays.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.auto_getconfig:
                        editor.putString("autoWay","getconfig");
                        break;
                    case R.id.auto_catch:
                        editor.putString("autoWay","catch");
                        break;
                    default:
                        break;
                }
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //存入数据
                editor.putString("backpath", backpath);
                editor.putInt("autotime", Integer.parseInt(autotime.getText().toString()));
                editor.putBoolean("doset", true);
                editor.putBoolean("autoCheckAfterScreenOn", autoCheckAfterScreenOn.isChecked());
                editor.putBoolean("screenOff", screenOff.isChecked());
                editor.putBoolean("changeOpen", changeOpen.isChecked());
                editor.putBoolean("hide",hide.isChecked());
                editor.putBoolean("iceBrowser",iceBrowser.isChecked());
                editor.putBoolean("openTask",openTask.isChecked());
                editor.putBoolean("onlyCheckIp",onlyCheckIp.isChecked());
                editor.commit();
                Toast.makeText(getBaseContext(), "保存成功,部分设置重启生效", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    //启动函数
    public void settingStart() {
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        autoCheckAfterScreenOn.setChecked(sp.getBoolean("autoCheckAfterScreenOn", true));
        screenOff.setChecked(sp.getBoolean("screenOff", false));
        changeOpen.setChecked(sp.getBoolean("changeOpen", false));
        iceBrowser.setChecked(sp.getBoolean("iceBrowser",false));
        onlyCheckIp.setChecked(sp.getBoolean("onlyCheckIp",false));
        autotime.setText(sp.getInt("autotime", 30) + "");
        backpath = sp.getString("backpath", null);
        openTask.setChecked(sp.getBoolean("openTask",true));
        switch (sp.getString("ipWay","helper")){
            case "shell":
                ipway.setText("终端");
                break;
            case "helper":
                ipway.setText("助手");
                break;
            case "browser":
                ipway.setText("浏览器");
                break;
            case "not":
                ipway.setText("不查询");
                break;
        }   
        switch (sp.getString("autoWay","getconfig")){
            case "getconfig":
                RadioButton radioButton = (RadioButton)findViewById(R.id.auto_getconfig);
                radioButton.setChecked(true);
                break;
            case "catch":
                RadioButton radioButton2 = (RadioButton)findViewById(R.id.auto_catch);
                radioButton2.setChecked(true);
                break;
        }
        hide.setChecked(sp.getBoolean("hide",false));
        switch (sp.getString("ipPort","ipip")){
            case "ipip":
                ipPorts.setText("ipip");
                break;
            case "cip":
                ipPorts.setText("cip");
                break;
            case "cz88":
                ipPorts.setText("纯真");
                break;
            case "pconline":
                ipPorts.setText("pconline");
                break;
            default:
                break;
        }

    }

}
