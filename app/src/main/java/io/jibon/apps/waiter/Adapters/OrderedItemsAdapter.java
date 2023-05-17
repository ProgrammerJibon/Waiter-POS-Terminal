package io.jibon.apps.waiter.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import io.jibon.apps.waiter.CustomTools;
import io.jibon.apps.waiter.Internet2;
import io.jibon.apps.waiter.R;

public class OrderedItemsAdapter extends RecyclerView.Adapter<OrderedItemsAdapter.OrderedItemsHolder> {
    public JSONArray userList;
    public Activity activity;
    public CustomTools customTools;

    public OrderedItemsAdapter(Activity activity, JSONArray userList) {
        this.userList = userList;
        this.activity = activity;
        this.customTools = new CustomTools(activity);
    }

    @Override
    public OrderedItemsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ordered_items, parent, false);
        OrderedItemsHolder viewHolder = new OrderedItemsHolder(view);
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(OrderedItemsHolder holder, int position) {
        try {
            JSONObject item = userList.getJSONObject(position);
            holder.itemSL.setText(String.valueOf(position+1));
            holder.itemName.setText(item.getString("name_then"));
            if (!item.getBoolean("printed")){
                holder.itemName.setTextColor(Color.RED);
            }else{
                holder.itemName.setTextColor(Color.GRAY);
            }
            holder.itemQuantity.setText(item.getString("item_quantity"));
            holder.itemPrice.setText(item.getString("price_then"));
            float TOTAL = Float.parseFloat(item.getString("item_quantity")) * Float.parseFloat(item.getString("price_then"));
            holder.itemTotal.setText(String.format("%.2f",TOTAL));
            holder.removeItemBtn.setOnClickListener(v -> {
                try {
                    String ipAddress = customTools.setPref("serverIpAddress", null);
                    String connectorCode = customTools.setPref("connectorCode", null);
                    String url = "http://" + ipAddress + "/json/app?connectorCode=" + connectorCode + "&removeOrderedItem=" + item.getString("order_item_id");
                    Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
                        try {
                            if (result.has("removeOrderedItem")) {
                                if (result.getBoolean("removeOrderedItem")){
                                    userList.remove(position);
                                    notifyDataSetChanged();
                                }
                            }
                        } catch (Exception error) {
                            Log.e("errnos", error.getMessage());
                        }
                    }));
                    internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }catch (Exception ignored){}
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

    public static class OrderedItemsHolder extends RecyclerView.ViewHolder {
        public TextView itemSL, itemName, itemPrice, itemQuantity, itemTotal;
        public ImageView removeItemBtn;

        public OrderedItemsHolder(View view) {
            super(view);
            itemSL = view.findViewById(R.id.itemSL);
            itemName = view.findViewById(R.id.itemName);
            itemQuantity = view.findViewById(R.id.itemQuantity);
            itemPrice = view.findViewById(R.id.itemPrice);
            itemTotal = view.findViewById(R.id.itemTotal);
            removeItemBtn = view.findViewById(R.id.removeItemBtn);
            removeItemBtn.getLayoutParams().height = 100;
        }
    }
}
