package com.section41.whatsapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.section41.whatsapp.R;
import com.section41.whatsapp.model.Chat;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
private Context context;
private List<Chat> chatList;
private String imgURL;

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    FirebaseUser fUser;


    public MessageAdapter(Context context, List<Chat> chatList, String imgURL) {
        this.context = context;
        this.chatList = chatList;
        this.imgURL= imgURL;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==MSG_TYPE_RIGHT){
                   view = LayoutInflater.from(context).inflate(R.layout.chat_item_right , parent,false);

                }else {
                    view = LayoutInflater.from(context).inflate(R.layout.chat_item_left , parent,false);

                }


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Chat chat =chatList.get(position);
            holder.show_message.setText(chat.getMessage());
            if(imgURL.equals("default")||imgURL.equals("")){
                holder.profile_img.setImageResource(R.mipmap.ic_launcher);
            }else {
                Glide.with(context)
                        .load(imgURL)
                        .into(holder.profile_img);
            }

        Log.d("myTag","is seen: "+chat.isSeen());

            if(position == chatList.size()-1 ){
                if(chat.isSeen()){
                    holder.tvIsSeen.setText("Seen");
                }else {
                    holder.tvIsSeen.setText("Delivered");

                }
                holder.tvIsSeen.setVisibility(View.VISIBLE);

            }else {
                holder.tvIsSeen.setVisibility(View.GONE);

            }

    }

    @Override
    public int getItemCount() {
        return  chatList == null ? 0 : chatList.size();
    }


public class MyViewHolder extends RecyclerView.ViewHolder{
    TextView show_message;
    ImageView profile_img;
    TextView tvIsSeen;
        public MyViewHolder(@NonNull View itemView) {
        super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_img = itemView.findViewById(R.id.profile_img);
            tvIsSeen = itemView.findViewById(R.id.tvIsSeen);
    }
}

    @Override
    public int getItemViewType(int position) {
      fUser= FirebaseAuth.getInstance().getCurrentUser();
      // if currentUser is sender , set viewType to msg_type_right means will display with chat_item_right.xml
      if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
      }else {
          return MSG_TYPE_LEFT;
      }
    }
}
