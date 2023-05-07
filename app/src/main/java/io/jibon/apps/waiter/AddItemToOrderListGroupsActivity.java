package io.jibon.apps.waiter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import org.json.JSONArray;

import io.jibon.apps.waiter.Adapters.ItemGroupsListAdapter;

public class AddItemToOrderListGroupsActivity extends AppCompatActivity {
    public Activity activity;
    public String order_id="";
    public RecyclerView my_recycler_view;
    public JSONArray jsonArray = new JSONArray();
    public ItemGroupsListAdapter itemGroupsListAdapter;
    public String ipAddress = "127.0.0.1";
    public String connectorCode = "0";
    public CustomTools customTools;
    public RelativeLayout progressCircular;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        customTools = new CustomTools(activity);
        setContentView(R.layout.activity_add_item_to_order_list_groups);

        ipAddress = customTools.setPref("serverIpAddress", null);
        connectorCode = customTools.setPref("connectorCode", null);

        //find view by id
        my_recycler_view = activity.findViewById(R.id.my_recycler_view);
        progressCircular = activity.findViewById(R.id.progressCircular);

        Bundle bundle = activity.getIntent().getExtras();
        if (bundle != null){
            order_id = bundle.getString("order_id");
            my_recycler_view.setLayoutManager(new LinearLayoutManager(this));
            itemGroupsListAdapter = new ItemGroupsListAdapter(activity, new JSONArray(), order_id);
            my_recycler_view.setAdapter(itemGroupsListAdapter);
            loadItemsGroupName();
        }
    }
    public  void loadItemsGroupName(){
        progressCircular.setVisibility(View.VISIBLE);
        String url = "http://"+ipAddress+"/json/app?connectorCode="+connectorCode+"&groupList="+order_id;
        Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
            progressCircular.setVisibility(View.GONE);
            try {
                if (result.has("groupList")) {
                    itemGroupsListAdapter.updateData(result.getJSONArray("groupList"));
                }
            }catch (Exception e){
                Log.e("errnos", e.getMessage());
            }
        }));
        internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}