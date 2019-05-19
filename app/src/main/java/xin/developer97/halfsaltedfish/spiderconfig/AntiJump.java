package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class AntiJump extends AppCompatActivity {
    private String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_anti_jump);
        Button button = (Button)findViewById(R.id.save);
        Button lookuid = (Button)findViewById(R.id.lookuid);
        final EditText editText = (EditText)findViewById(R.id.jump);
        final Tools tools = new Tools(getApplicationContext());
        try {
            path = tools.GetFiles(getApplicationContext().getFilesDir()+"", ".ini", false).get(0);
            editText.setText(tools.readFromSD(path));
        } catch (Exception e) {
            e.printStackTrace();
            path = getApplicationContext().getFilesDir()+"/core.ini";
            try {
                tools.mes("打开失败，使用固定模式");
                editText.setText(tools.readFromSD(path));
            } catch (IOException e1) {
                e1.printStackTrace();
                path = getApplicationContext().getFilesDir()+"/start.ch";
                try {
                    editText.setText(tools.readFromSD(path));
                } catch (IOException e2) {
                    e2.printStackTrace();
                    tools.mes("打开失败,没装脚本?");
                }
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
        lookuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AntiJump.this, uid.class);
                startActivity(intent);
            }
        });
    }
}
