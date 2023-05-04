package io.jibon.apps.waiter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.jibon.apps.waiter.Adapters.UserAdapter;

public class TablesSelector extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    public Activity activity;
    public String ipAddress = "127.0.0.1";
    public String connectorCode = "0";
    public CustomTools customTools;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        customTools = new CustomTools(activity);
        setContentView(R.layout.activity_tables_selector);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(activity, new JSONArray());
        recyclerView.setAdapter(userAdapter);

        // mechanism
        ipAddress = customTools.setPref("serverIpAddress", null);
        connectorCode = customTools.setPref("connectorCode", null);
        loadTableList();
    }

    public void loadTableList(){
        String url = "http://"+ipAddress+"/json/app?connectorCode="+connectorCode+"&lists=tables";
        Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
            try {
                if (result.has("lists_table")) {
                    userAdapter.updateData(result.getJSONArray("lists_table"));
                }
            }catch (Exception error){
                Log.e("errnos", error.getMessage());
            }
        }));
        internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}