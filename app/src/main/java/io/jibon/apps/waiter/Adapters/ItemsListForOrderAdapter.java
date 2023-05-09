package io.jibon.apps.waiter.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import io.jibon.apps.waiter.CustomTools;
import io.jibon.apps.waiter.Internet2;
import io.jibon.apps.waiter.R;

public class ItemsListForOrderAdapter extends RecyclerView.Adapter<ItemsListForOrderAdapter.ItemsListForOrder> {
    private JSONArray userList;
    public Activity activity;
    public String order_id;
    public String ipAddress = "127.0.0.1", connectorCode = "0";
    public CustomTools customTools;

    public ItemsListForOrderAdapter(Activity activity, JSONArray userList, String order_id) {
        this.userList = userList;
        this.order_id = order_id;
        this.activity = activity;
        this.customTools = new CustomTools(activity);
        this.ipAddress = customTools.setPref("serverIpAddress", null);
        this.connectorCode = customTools.setPref("connectorCode", null);
    }

    @Override
    public ItemsListForOrder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_items_list_for_order, parent, false);
        ItemsListForOrder viewHolder = new ItemsListForOrder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ItemsListForOrder holder, int position) {
        try {
            JSONObject item = userList.getJSONObject(position);
            String itemName = item.getString("item_name");
            Float itemPrice = Float.parseFloat(item.getString("item_price"));
            holder.nameOfItem.setText(itemName);
            holder.priceOfItem.setText("€" + itemPrice);

            holder.orderButton.setOnClickListener(v -> {
                int quantity = Integer.parseInt(String.valueOf(holder.quantityOfItem.getText()));
                if (quantity > 0){
                    holder.parent.setAlpha((float) 0.5);
                    holder.orderButton.setClickable(false);
                    holder.quantityOfItem.setFocusable(false);

                    String url = "http://"+ipAddress+"/json/app?connectorCode="+connectorCode+"&place_order="+order_id+"&item_name="+itemName+"&item_quantity="+quantity+"&item_price="+itemPrice;
                    Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
                        try {
                            if (result.has("place_order")){
                                if (result.getBoolean("place_order")){
                                    customTools.toast("Added Successfully");
                                }else {
                                    customTools.toast("Something Went Wrong!");
                                    holder.parent.setAlpha((float) 1);
                                    holder.orderButton.setClickable(true);
                                    holder.quantityOfItem.setFocusable(true);
                                }
                            }else{
                                customTools.toast("Unable to connect!");
                                holder.parent.setAlpha((float) 1);
                                    holder.orderButton.setClickable(true);
                                    holder.quantityOfItem.setFocusable(true);
                            }

                        }catch (Exception error){
                            Log.e("errnos", error.getMessage());
                        }
                    }));
                    internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    public static class ItemsListForOrder extends RecyclerView.ViewHolder {
        TextView nameOfItem, priceOfItem;
        EditText quantityOfItem;
        Button orderButton;
        LinearLayout parent;
        public ItemsListForOrder(View view) {
            super(view);
            try{
                parent = (LinearLayout) view;
                nameOfItem = view.findViewById(R.id.nameOfItem);
                priceOfItem = view.findViewById(R.id.priceOfItem);
                quantityOfItem = view.findViewById(R.id.quantityEditText);
                orderButton = view.findViewById(R.id.addBtn);
            }catch (Exception e){
                Log.e("errnos", e.getMessage());
            }
        }
    }
}