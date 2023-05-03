package io.jibon.apps.waiter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    protected EditText serverIpAddress, connectorCode;
    protected RelativeLayout progressCircular;
    protected Button connectBtn;
    protected Activity activity;
    protected CustomTools customTools;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_main);
        // find by id
        serverIpAddress = activity.findViewById(R.id.serverIpAddress);
        connectorCode = activity.findViewById(R.id.connectorCode);
        connectBtn = activity.findViewById(R.id.connectBtn);
        customTools = new CustomTools(activity);
        progressCircular = activity.findViewById(R.id.progressCircular);
        // set onclick listener
        connectBtn.setOnClickListener(v -> {
            String ipaddress = String.valueOf(serverIpAddress.getText());
            if (isIPAddress(ipaddress)){
                String code = getAcquiredCode(String.valueOf(connectorCode.getText()));
                if (code.length() == 8){
                    ipaddress = "http://"+ipaddress+"/json/app?connectorCode="+code;
                    connectToNetwork(ipaddress);
                }else{
                    customTools.toast("Incorrect Code!");
                    connectorCode.findFocus();
                }
            }else{
                customTools.toast("Incorrect IP address!");
                serverIpAddress.findFocus();
            }
        });
        // set default value
        serverIpAddress.setText(customTools.setPref("serverIpAddress", null));
        // get default values
        String prevServerIP = customTools.setPref("serverIpAddress", null);
        String prevConnectorCode = customTools.setPref("connectorCode", null);
        if (!prevServerIP.equals("") && !prevConnectorCode.equals("")){
            connectToNetwork("http://"+prevServerIP+"/json/app?connectorCode="+prevConnectorCode);
        }
    }

    protected void connectToNetwork(String url){
        progressCircular.setVisibility(View.VISIBLE);
        Internet2 connectToServer = new Internet2(activity, url, (code, result) -> {
            progressCircular.setVisibility(View.GONE);
            try {
                if (code == 200) {
                    customTools.setPref("serverIpAddress", String.valueOf(serverIpAddress.getText()));
                    if (result.has("connectionResult")) {
                        if (result.getBoolean("connectionResult")){
                            if (!String.valueOf(connectorCode.getText()).equals("")){
                                customTools.setPref("connectorCode", String.valueOf(connectorCode.getText()));
                            }
                            connectorCode.clearFocus();
                            connectorCode.setText("");
                            progressCircular.setVisibility(View.VISIBLE);
                            customTools.toast("Welcome back "+(result.has("connectionUsername")?result.getString("connectionUsername"):"!"));
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(activity, TablesSelector.class);
                                    activity.startActivity(intent);
                                    activity.finish();
                                }
                            }, 1000);
                        }else{
                            customTools.toast("Incorrect Code!", R.drawable.baseline_portable_wifi_off_24, R.color.gray);
                        }
                    }
                    Log.e("errnos", String.valueOf(result));

                } else {
                    customTools.toast("Incorrect IP address!", R.drawable.baseline_portable_wifi_off_24, R.color.orange);
                }
            }catch (Exception e){
                customTools.toast("Something went wrong!\n"+e.getMessage());
            }
        });
        connectToServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static String getAcquiredCode(String str){
        return str.replaceAll("[^0-9]", "");
    }


    public static boolean isIPAddress(String s) {
        String patternString = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }
}