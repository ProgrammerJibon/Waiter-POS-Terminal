package io.jibon.apps.waiter.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.jibon.apps.waiter.R;
import io.jibon.apps.waiter.TableFullScreenView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    protected JSONArray userList;
    protected Activity activity;

    public UserAdapter(Activity activity, JSONArray userList) {
        this.userList = userList;
        this.activity = activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(JSONArray newData) {
        this.userList = newData;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        try {
            JSONObject user = userList.getJSONObject(position);
            holder.userName.setText(user.getString("table_name"));
            if (user.has("current_status")){
                holder.statusIcon.setImageResource(R.drawable.baseline_access_time_24);
            }else{
                holder.statusIcon.setImageResource(0);
            }
            holder.parentLayout.setOnClickListener(v -> {
                Intent intent = new Intent(activity, TableFullScreenView.class);
                try {
                    intent.putExtra("tableID", user.getString("table_id"));
                    intent.putExtra("tableName", user.getString("table_name"));
                    if (user.has("current_status")){
                        JSONObject current_status = user.getJSONObject("current_status");
                        intent.putExtra("vat", current_status.getString("vat"));
                        intent.putExtra("order_id", current_status.getString("order_id"));
                        intent.putExtra("customer_name", current_status.getString("customer_name"));
                        intent.putExtra("customer_phone", current_status.getString("customer_phone"));
                        intent.putExtra("order_taker_name", current_status.getString("order_taker_name"));
                        intent.putExtra("order_status", current_status.getString("status"));
                        intent.putExtra("order_time", current_status.getString("order_time"));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                activity.startActivity(intent);
            });
        }catch (Exception error){
            Log.e("errnos", error.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return userList.length();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        protected TextView userName;
        protected ImageView statusIcon;
        protected LinearLayout parentLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            parentLayout = (LinearLayout) itemView;
        }
    }
}

