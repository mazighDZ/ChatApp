package com.section41.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.section41.whatsapp.Util.EncryptionUtil;

import com.section41.whatsapp.adapter.MessageAdapter;
import com.section41.whatsapp.entity.EncryptedLocalUser;
import com.section41.whatsapp.entity.EncryptedRemoteUser;
import com.section41.whatsapp.helper.Helper;
import com.section41.whatsapp.model.Chat;
import com.section41.whatsapp.model.User;
import com.section41.whatsapp.registration.RegistrationKeyModel;
import com.section41.whatsapp.registration.RegistrationManager;
import com.section41.whatsapp.singleConversation.EncryptedSingleSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;

public class MessageActivity extends AppCompatActivity {

    TextView username;
    ImageView userImage;

    FirebaseUser fUser;
    DatabaseReference myReference;
    DatabaseReference localReference;
    Intent intent;

    RecyclerView recyclerViewMsg;
    MessageAdapter messageAdapter;
    List<Chat> chatsList;
    EditText msg_EditText;
    ImageButton sendBtn;
    String userIdContacted;
    ValueEventListener seenListener;

    boolean isMessageActivityResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        userImage = findViewById(R.id.imageView_msg);
        username = findViewById(R.id.tvUsername_msg);

        sendBtn = findViewById(R.id.btn_send);
        msg_EditText = findViewById(R.id.text_send);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        // tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        intent = getIntent();

        userIdContacted = intent.getStringExtra("userId");



        localReference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());

        recyclerViewMsg = findViewById(R.id.recyclerView_msg);
        recyclerViewMsg.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMsg.setLayoutManager(linearLayoutManager);



//        initRemoteUserChatSession();

        myReference = FirebaseDatabase.getInstance().getReference("Users").child(userIdContacted);
        myReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    userImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext())
                            .load(user.getImageURL())
                            .into(userImage);
                }
                readMessages(fUser.getUid(), userIdContacted, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msg_EditText.getText().toString().trim();
                if (!msg.isEmpty()) {
                    sendMessage(fUser.getUid(),userIdContacted,msg);
                } else {
                    Toast.makeText(MessageActivity.this, "Please send a non-empty message", Toast.LENGTH_SHORT).show();
                }
                msg_EditText.setText("");
            }
        });

        seenMessage(userIdContacted);

    }




    private void readMessages(String myId, String receiverId, String receiverUrl) {
        chatsList = new ArrayList<>();
        DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference("Chats");
        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(receiverId) || chat.getReceiver().equals(receiverId) && chat.getSender().equals(myId)) {
                        chatsList.add(chat);
                    }
                    messageAdapter = new MessageAdapter(getApplicationContext(), chatsList, receiverUrl);
                    recyclerViewMsg.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String msg) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", msg);
        hashMap.put("isSeen", false);
        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fUser.getUid())
                .child(userIdContacted);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userIdContacted);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(String userIdTexted) {
        myReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = myReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (isMessageActivityResumed) {
                    boolean isAnyMessageUpdated = false; // Track if any message is updated

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);

                        if (chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userIdTexted)) {
                            if (!chat.isSeen()) { // Check if the message is not already marked as seen
                                dataSnapshot.getRef().child("isSeen").setValue(true); // Update the isSeen flag
                                isAnyMessageUpdated = true;
                            }
                        }
                    }

                    if (isAnyMessageUpdated) {
                        // Perform any necessary actions when a message is marked as seen
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkStatus(String status) {
        myReference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        myReference.updateChildren(hashMap);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // User clicked the "home" button in the toolbar

            finish(); // Finish the activity and go back
            return true;
        } else if (item.getItemId() == R.id.secretChatStart) {
            Intent i = new Intent(MessageActivity.this, SecretMessageActivity.class);
            i.putExtra("userId",userIdContacted);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        isMessageActivityResumed = true;
        checkStatus("online");
        seenMessage(userIdContacted); // Update the seen status only when the activity is resumed

    }

    @Override
    protected void onPause() {
        super.onPause();
        isMessageActivityResumed = false;
        myReference.removeEventListener(seenListener);
        checkStatus("Offline");
    }

}
