package io.jibon.apps.waiter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {
    protected EditText adminUsername, connectorCode;
    String prevServerIP = "127.0.0.1", prevUsername = "";
    int  licenseTime = 0;
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
//        adminUsername = activity.findViewById(R.id.adminUsername);
        connectorCode = activity.findViewById(R.id.connectorCode);
        connectBtn = activity.findViewById(R.id.connectBtn);
        customTools = new CustomTools(activity);
        progressCircular = activity.findViewById(R.id.progressCircular);
        // set onclick listener
        connectBtn.setOnClickListener(v -> {
            progressCircular.setVisibility(View.VISIBLE);
            String admin_user_name = adminUsername.getText().toString();
            String licenseUri = "https://www.jibon.io/verify_license.php?username="+admin_user_name;
            Internet2 internet = new Internet2(activity, licenseUri, ((code1, res) -> {
                progressCircular.setVisibility(View.GONE);
                try {
                    if (code1 == 200){
                        if (res.has("result_username")) {
                            JSONObject result_username = res.getJSONObject("result_username");
                            licenseTime = customTools.licenseTime(Integer.parseInt(result_username.getString("valid_till")));
                            customTools.setPref("admin_username", result_username.getString("username"));
                            prevServerIP = customTools.setPref("serverIpAddress", result_username.getString("last_ip"));
                            connectToNetwork("http://"+prevServerIP+"/json/app?connectorCode="+connectorCode.getText());
                        }else{
                            customTools.toast("Incorrect Username");
                        }
                    }else{
                        customTools.alert("Error!", "Cannot connect to internet.");
                    }
                }catch (Exception e){
                    Log.e("errnos", e.getMessage());
                }
            }));
            internet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        });

        // get default values
        prevServerIP = customTools.setPref("serverIpAddress", null);
        String prevConnectorCode = customTools.setPref("connectorCode", null);
        licenseTime = customTools.licenseTime();
        prevUsername = customTools.setPref("admin_username", null);
        if (!prevUsername.equals("") && !prevServerIP.equals("") && !prevConnectorCode.equals("")){
            connectToNetwork("http://"+prevServerIP+"/json/app?connectorCode="+prevConnectorCode);
        }
        // set default value
        adminUsername.setText(prevUsername);
        connectorCode.setText(prevConnectorCode);
    }

    protected void connectToNetwork(String url){
        progressCircular.setVisibility(View.VISIBLE);
        Date date = new Date();
        if (licenseTime < date.getTime()/1000){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Trial Expired!")
                    .setMessage("Please buy license from your seller.")
                    .setPositiveButton("Buy", ((dialog, which) -> {
                        dialog.cancel();
                        activity.finish();
                    }))
                    .setNegativeButton("Exit", ((dialog, which) -> {
                        dialog.cancel();
                        activity.finish();
                    }));
            builder.create().show();
            return;
        }
        Internet2 connectToServer = new Internet2(activity, url, (code, result) -> {
            progressCircular.setVisibility(View.GONE);
            try {
                if (code == 200) {
                    if (result.has("connectionResult")) {
                        if (result.getBoolean("connectionResult")){
                            if (!String.valueOf(connectorCode.getText()).equals("")){
                                customTools.setPref("connectorCode", String.valueOf(connectorCode.getText()));
                            }
                            customTools.toast("Welcome back "+(result.has("connectionUsername")?result.getString("connectionUsername"):"!"));
                            Intent intent = new Intent(activity, TablesSelector.class);
                            activity.startActivity(intent);
                            activity.finish();
                        }else{
                            customTools.toast("Incorrect Code!", R.drawable.baseline_portable_wifi_off_24, R.color.gray);
                        }
                    }

                } else {
                    customTools.alert("Incorrect Device address!", "If you change your wifi name, password or change your device. Please contact with your app distributor.");
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