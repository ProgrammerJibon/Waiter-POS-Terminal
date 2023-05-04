package io.jibon.apps.waiter.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.jibon.apps.waiter.R;

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
            Log.e("errnos", String.valueOf(user));
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

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
        }
    }
}

