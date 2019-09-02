package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class set extends AppCompatActivity {
    private SharedPreferences sp;
    private String backpath;
    EditText autotime, ip ,Number_of_packages;
    Switch hide, autoCheckAfterScreenOn, screenOff, changeOpen, openTask,iceBrowser,onlyCheckIp;
    Tools tools = Tools.getTools();

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
        RadioGroup ipGroup = (RadioGroup) findViewById(R.id.ipGroup);
        RadioGroup ipWays = (RadioGroup) findViewById(R.id.ipWays);
        RadioGroup autoWays = (RadioGroup) findViewById(R.id.autoWays);
        RadioGroup ipPorts = (RadioGroup) findViewById(R.id.ipPorts);

        autotime = (EditText) findViewById(R.id.autotime);
        Number_of_packages = (EditText)findViewById(R.id.Number_of_packages);
        ip = (EditText) findViewById(R.id.ip);

        Button background = (Button) findViewById(R.id.background);
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


        //获取选中ip
        ipGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radbtn = (RadioButton) findViewById(checkedId);
                ip.setText(radbtn.getText());
            }
        });
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
        ipWays.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.check_ip_by_shell:
                        editor.putString("ipWay","shell");
                        break;
                    case R.id.check_ip_by_helper:
                        editor.putString("ipWay","helper");
                        break;
                    case R.id.check_ip_by_browser:
                        editor.putString("ipWay","browser");
                        break;
                    case R.id.not_check_ip:
                        editor.putString("ipWay","not");
                        break;
                    default:
                        break;
                }
            }
        });
        //ip查询接口
        ipPorts.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.check_ip_use_ipip:
                        editor.putString("ipPort","ipip");
                        break;
                    case R.id.check_ip_use_cip:
                        editor.putString("ipPort","cip");
                        break;
                    case R.id.check_use_ipcn:
                        editor.putString("ipPort","cz88");
                        break;
                    case R.id.check_use_pconline:
                        editor.putString("ipPort","pconline");
                        break;
                    default:
                        break;
                }
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
                editor.putString("Number_of_packages",Number_of_packages.getText().toString());
                editor.putString("ip", ip.getText().toString());
                editor.putBoolean("doset", true);
                editor.putBoolean("autoCheckAfterScreenOn", autoCheckAfterScreenOn.isChecked());
                editor.putBoolean("screenOff", screenOff.isChecked());
                editor.putBoolean("changeOpen", changeOpen.isChecked());
                editor.putBoolean("openTask", openTask.isChecked());
                editor.putBoolean("hide",hide.isChecked());
                editor.putBoolean("iceBrowser",iceBrowser.isChecked());
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
        Number_of_packages.setText(sp.getString("Number_of_packages","5"));
        ip.setText(sp.getString("ip", "157.255.173.182"));
        backpath = sp.getString("backpath", null);
        openTask.setChecked(sp.getBoolean("openTask",true));
        switch (sp.getString("ipWay","shell")){
            case "shell":
                RadioButton radioButton = (RadioButton)findViewById(R.id.check_ip_by_shell);
                radioButton.setChecked(true);
                break;
            case "helper":
                RadioButton radioButton2 = (RadioButton)findViewById(R.id.check_ip_by_helper);
                radioButton2.setChecked(true);
                break;
            case "browser":
                RadioButton radioButton3 = (RadioButton)findViewById(R.id.check_ip_by_browser);
                radioButton3.setChecked(true);
                break;
            case "not":
                RadioButton radioButton4 = (RadioButton)findViewById(R.id.not_check_ip);
                radioButton4.setChecked(true);
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
                RadioButton radioButton = (RadioButton)findViewById(R.id.check_ip_use_ipip);
                radioButton.setChecked(true);
                break;
            case "cip":
                RadioButton radioButton2 = (RadioButton)findViewById(R.id.check_ip_use_cip);
                radioButton2.setChecked(true);
                break;
            case "cz88":
                RadioButton radioButton3 = (RadioButton)findViewById(R.id.check_use_ipcn);
                radioButton3.setChecked(true);
                break;
            case "pconline":
                RadioButton radioButton4 = (RadioButton)findViewById(R.id.check_use_pconline);
                radioButton4.setChecked(true);
                break;
            default:
                break;
        }

    }

}
