package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewConfig {

    private String time="";
    private String proxy="";
    private String guid="";
    private String token = "";
    private SharedPreferences sp;
    private static NewConfig newConfig = new NewConfig();

    private NewConfig(){
        Context context = MyApplication.getInstance().getApplicationContext();
        sp = context.getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        Tools tools = Tools.getTools();
        try {
            Matcher matcher = Pattern.compile("Q-Token:(\\w+?)[\\s,\\r\\n]").matcher(tools.readFromSD(context.getFilesDir()+"/tiny.conf"));
            if(matcher.find()){
                this.token = matcher.group(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static NewConfig getNewConfig(){
        return newConfig;
    }

    //QQ模式
    public String generateConfig(String time,String guid, String token){
        this.time = time;
        this.guid = decryptGuid(guid);
        this.token = decryptToken(token);
        return getConfig();
    }
    //UC模式
    public String generateUCConfig(String time,String proxy){
        this.time = time;
        this.proxy = decryptToken(proxy);
        return getConfig();
    }

    private String getConfig(){
        if(sp.getString("dynamic","QQ").equals("QQ")){
            String config = sp.getString("module","listen_port=65080;\n" +
                    "worker_proc=1;\n" +
                    "daemon=on;\n" +
                    "uid=3004;\n" +
                    "mode=wap;\n" +
                    "\n" +
                    "http_ip=157.255.173.182;\n" +
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
                    "https_ip=157.255.173.182;\n" +
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
                    "dns_url=\"119.29.29.29\";\n");
            return config.replace("替换GUID",guid).replace("替换Token",token);
        }else if (sp.getString("dynamic","QQ").equals("UC")){
            return sp.getString("module","").replace("代码",proxy);
        }
        return "";
    }

    public String getTime() {
        return time;
    }
    public String getGuid() {
        return guid;
    }
    public String getToken() {
        return token;
    }
    public String getProxy() {
        return proxy;
    }

    private char password(int num){
        switch (num){
            case 0:
                return 'a';
            case 1:
                return 'b';
            case 2:
                return 'c';
            case 3:
                return 'd';
            case 4:
                return 'e';
            case 5:
                return 'f';
            case 6:
                return '0';
            case 7:
                return '1';
            case 8:
                return '2';
            case 9:
                return '4';
        }
        return '8';
    }
    //解密GUID
    public String decryptGuid(String decryptGuid){
        Log.i("Guid替换数字",time.charAt(time.length()-1)+" "+time);
        return decryptGuid.replace('*',password((int)time.charAt(time.length()-1)));
    }
    //解密Token
    public String decryptToken(String decryptToken){
        Log.i("Token替换数字",time.charAt(time.length()-2)+" "+time);
        return decryptToken.replace('*',password((int)time.length()-2));
    }
}
