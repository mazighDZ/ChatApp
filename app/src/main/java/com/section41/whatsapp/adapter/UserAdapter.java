package com.section41.whatsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.section41.whatsapp.MessageActivity;
import com.section41.whatsapp.R;
import com.section41.whatsapp.SecretMessageActivity;
import com.section41.whatsapp.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
private Context context;
private List<User> mUsers;
private boolean isChat;

    public UserAdapter(Context context, List<User> mUsers ,boolean isChat) {
        this.context = context;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        if(user.getImageURL().equals("default")) {
    holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide
                    .with(context)
                    .load(user.getImageURL())
                    .centerCrop()
                    .into(holder.imageView);
        }
        //check status
        if(isChat){
            if(user.getStatus().equals("online")){
                holder.olineStatusIV.setVisibility(View.VISIBLE);
                holder.offlineStatusIV.setVisibility(View.GONE);
            }else {
                holder.olineStatusIV.setVisibility(View.GONE);
                holder.offlineStatusIV.setVisibility(View.VISIBLE);
            }
        }else{
            holder.olineStatusIV.setVisibility(View.GONE);
            holder.offlineStatusIV.setVisibility(View.GONE);
        }


        //handel when user click in one other users
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(context, MessageActivity.class);// test for secret chat
                Intent i = new Intent(context, MessageActivity.class);// test for secret chat
                i.putExtra("userId",user.getId());
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    //viewHolder
    class MyViewHolder extends  RecyclerView.ViewHolder{
        public TextView username;
        public ImageView imageView;
        public ImageView olineStatusIV;
        public ImageView offlineStatusIV;
        public MyViewHolder(@NonNull View view) {
            super(view);
            username = view.findViewById(R.id.tvUsername);
            imageView = view.findViewById(R.id.imageView);
            olineStatusIV = view.findViewById(R.id.ivOnline);
            offlineStatusIV = view.findViewById(R.id.ivOffline);
        }
    }
}
