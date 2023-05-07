package io.jibon.apps.waiter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import io.jibon.apps.waiter.Adapters.ItemGroupsListAdapter;
import io.jibon.apps.waiter.Adapters.ItemsListForOrderAdapter;

public class AddItemsToOrderItemListActivity extends AppCompatActivity {
    public TextView pageTitle;
    public Activity activity;
    public CustomTools customTools;
    public String ipAddress, connectorCode, order_id, group_id, group_name;
    public ItemsListForOrderAdapter itemsListForOrderAdapter;
    public RecyclerView my_recycler_view;
    public RelativeLayout progressCircular;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        customTools = new CustomTools(activity);
        setContentView(R.layout.activity_add_items_to_order_item_list);

        // find view by id
        pageTitle = activity.findViewById(R.id.pageTitle);
        my_recycler_view = activity.findViewById(R.id.my_recycler_view);
        progressCircular = activity.findViewById(R.id.progressCircular);

        ipAddress = customTools.setPref("serverIpAddress", null);
        connectorCode = customTools.setPref("connectorCode", null);

        Bundle bundle = activity.getIntent().getExtras();
        if (bundle != null){
            order_id = bundle.getString("order_id");
            group_id = bundle.getString("group_id");
            group_name = bundle.getString("group_name");

            my_recycler_view.setLayoutManager(new LinearLayoutManager(this));
            itemsListForOrderAdapter = new ItemsListForOrderAdapter(activity, new JSONArray(), order_id);
            my_recycler_view.setAdapter(itemsListForOrderAdapter);

            pageTitle.setText(group_name);

            loadItemsList();
        }
    }

    private void loadItemsList() {
        progressCircular.setVisibility(View.VISIBLE);
        String url = "http://"+ipAddress+"/json/app?connectorCode="+connectorCode+"&itemsList=1&group_id="+group_id;
        Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
            progressCircular.setVisibility(View.GONE);
            try {
                if (result.has("itemsList")) {
                    itemsListForOrderAdapter.updateData(result.getJSONArray("itemsList"));
                }
            }catch (Exception e){
                Log.e("errnos", e.getMessage());
            }
        }));
        internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}