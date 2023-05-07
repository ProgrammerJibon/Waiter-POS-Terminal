package io.jibon.apps.waiter.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import io.jibon.apps.waiter.AddItemsToOrderItemListActivity;
import io.jibon.apps.waiter.R;

public class ItemGroupsListAdapter extends RecyclerView.Adapter<ItemGroupsListAdapter.ItemGroupsListHolder> {
    private JSONArray userList;
    public Activity activity;
    public String order_id;

    public ItemGroupsListAdapter(Activity activity, JSONArray userList, String order_id) {
        this.userList = userList;
        this.order_id = order_id;
        this.activity = activity;
    }

    @Override
    public ItemGroupsListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_items_groups, parent, false);
        ItemGroupsListHolder viewHolder = new ItemGroupsListHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ItemGroupsListHolder holder, int position) {
        try {
            JSONObject item = userList.getJSONObject(position);
            holder.groupName.setText(item.getString("group_name"));
            holder.parent.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(activity, AddItemsToOrderItemListActivity.class);
                    intent.putExtra("order_id", order_id);
                    intent.putExtra("group_id", item.getString("group_id"));
                    intent.putExtra("group_name", item.getString("group_name"));
                    activity.startActivity(intent);
                }catch (Exception e){
                    Log.e("errnos", e.getMessage());
                }
            });
        }catch (Exception e ){
            Log.e("errnos", e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return userList.length();
    }

    public void updateData(JSONArray newData) {
        this.userList = newData;
        notifyDataSetChanged();
    }

    public static class ItemGroupsListHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        LinearLayout parent;
        public ItemGroupsListHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.groupName);
            parent = (LinearLayout) view;
        }
    }
}