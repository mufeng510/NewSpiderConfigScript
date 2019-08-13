package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

public class Modle extends AppCompatActivity {
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_modle);
        SharedPreferences sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        //自定义壁纸
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layoutmodle);
        linearLayout.setBackgroundResource(R.mipmap.tree);
        Button button = (Button)findViewById(R.id.saveModle);
        final EditText editText = (EditText)findViewById(R.id.modletxt);
        final Tools tools = Tools.getTools();
        try {
            path  = tools.GetFiles(getApplicationContext().getFilesDir()+"", ".conf", false).get(0);
            editText.setText(tools.readFromSD(path));
        } catch (Exception e) {
            e.printStackTrace();
            path = getApplicationContext().getFilesDir() + "/tiny.conf";
            try {
                tools.mes("打开失败，使用固定模式");
                editText.setText(tools.readFromSD(path));
            } catch (IOException e1) {
                e1.printStackTrace();
                tools.mes("没有模式文件吧，去文件管理器看看");
            }
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    tools.savaFileToSD(path,editText.getText().toString());
                    Toast.makeText(getBaseContext(),"保存成功",Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(),"保存失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
