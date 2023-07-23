package com.section41.whatsapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.section41.whatsapp.adapter.MessageAdapter;
import com.section41.whatsapp.entity.EncryptedLocalUser;
import com.section41.whatsapp.entity.EncryptedRemoteUser;
import com.section41.whatsapp.model.Chat;
import com.section41.whatsapp.model.User;
import com.section41.whatsapp.registration.RegistrationKeyModel;
import com.section41.whatsapp.registration.RegistrationManager;
import com.section41.whatsapp.singleConversation.EncryptedSingleSession;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SecretMessageActivity extends AppCompatActivity {
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
    public static int DEFAULT_DEVICE_ID = 2;

    SharedPreferences sharedPreferences;

    List<Chat> localChatsList = new ArrayList<>();

    RegistrationKeyModel localUserModel;
    RegistrationKeyModel remoteUserModel;
    private EncryptedSingleSession localUserEncryptedSession;
    boolean isSecretMessageActivityResumed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_message);
        userImage = findViewById(R.id.imageView_msg_secret);
        username = findViewById(R.id.tvUsername_msg_secret);

        sendBtn = findViewById(R.id.btn_send_secret);
        msg_EditText = findViewById(R.id.text_send_secret);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            fUser = auth.getCurrentUser();
            // Rest of your code
        } else {
            // Redirect the user to the login screen or handle the authentication flow
            Intent i = new Intent(SecretMessageActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        // Shared preferences
        sharedPreferences = getSharedPreferences("SecretChatPreferences", Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString("chatSecretDeviceStorage", null);
        if (jsonString != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Chat>>() {}.getType();
            localChatsList = gson.fromJson(jsonString, type);
        }

        // Toolbar
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

        initRemoteUserModel();
        initLocalUserModel();




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


    }

    private void initLocalUserModel() {
        localReference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        localReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getIdentityKeyPairString() != null) {
                        IdentityKeyPair identityKeyPair = user.getIdentityKeyPair();
                        int registrationId = user.getRegistrationId();
                        SignedPreKeyRecord signedPreKeyRecord = user.getSignedPreKeyRecordObject();
                        List<PreKeyRecord> preKeys = user.getPreKeysListAsList();

                        // Create the RegistrationKeyModel object
                        localUserModel = new RegistrationKeyModel(identityKeyPair, registrationId, preKeys, signedPreKeyRecord);
                        initLocalUserChatSession();
                    }
                } else {
                    // Handle the case when the user data is not found in the database
                    // You can display an error message or take appropriate action
                    Log.d("myTag", "User data not found in the database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error when the database operation is canceled
                // You can display an error message or take appropriate action
                Log.d("myTag", "Database operation canceled: " + error.getMessage());
            }
        });
    }

    private void initRemoteUserModel() {
        myReference = FirebaseDatabase.getInstance().getReference("Users").child(userIdContacted);
        myReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User contactedUser = snapshot.getValue(User.class);
                    int registrationId = contactedUser.getRegistrationId();
                    List<PreKeyRecord> preKeys = contactedUser.getPreKeysListAsList();
                    SignedPreKeyRecord signedPreKeyRecord = contactedUser.getSignedPreKeyRecordObject();
                    IdentityKeyPair identityKeyPair = contactedUser.getIdentityKeyPair();

                    remoteUserModel = new RegistrationKeyModel(identityKeyPair, registrationId, preKeys, signedPreKeyRecord);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initLocalUserChatSession() {
        try {
            EncryptedLocalUser localUserModelEncrypted = new EncryptedLocalUser(
                    localUserModel.getIdentityKeyPair().serialize(),
                    localUserModel.getRegistrationId(),
                    fUser.getUid(),
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    localUserModel.getPreKeysBytes(),
                    localUserModel.getSignedPreKeyRecord().serialize()
            );

            EncryptedRemoteUser remoteUserModelEncrypted = new EncryptedRemoteUser(
                    remoteUserModel.getRegistrationId(),
                    userIdContacted,
                    RegistrationManager.DEFAULT_DEVICE_ID,
                    remoteUserModel.getPreKeys().get(0).getId(),
                    remoteUserModel.getPreKeys().get(0).getKeyPair().getPublicKey().serialize(),
                    remoteUserModel.getSignedPreKeyId(),
                    remoteUserModel.getSignedPreKeyRecord().getKeyPair().getPublicKey().serialize(),
                    remoteUserModel.getSignedPreKeyRecord().getSignature(),
                    remoteUserModel.getIdentityKeyPair().getPublicKey().serialize()
            );

            localUserEncryptedSession = new EncryptedSingleSession(
                    localUserModelEncrypted,
                    remoteUserModelEncrypted
            );


            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg = msg_EditText.getText().toString().trim();
                    if (!msg.isEmpty()) {

                        sendEncryptedMessage(msg);

                    } else {
                        Toast.makeText(SecretMessageActivity.this, "Please send a non-empty message", Toast.LENGTH_SHORT).show();
                    }
                    msg_EditText.setText("");
                }
            });

        } catch (InvalidKeyException | IOException | UntrustedIdentityException e) {
            e.printStackTrace();
        }
    }

    private void readMessages(String myId, String receiverId, String receiverUrl) {
        chatsList = new ArrayList<>();

        DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference("SecretChats");
        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(receiverId) ||
                            chat.getReceiver().equals(receiverId) && chat.getSender().equals(myId)) {
                        chatsList.add(chat);
                        if (!chat.isSeen()) {
                            Log.d("myTag" , "chant not seen :" + chat.getMessage());
                            readNewMessage(chat);
                        }
                    }

                }

                messageAdapter = new MessageAdapter(getApplicationContext(), localChatsList, receiverUrl);
                recyclerViewMsg.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(String userIdTexted) {
        myReference = FirebaseDatabase.getInstance().getReference("SecretChats");
        seenListener = myReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isSecretMessageActivityResumed) {
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


    private void sendEncryptedMessage(String msg) {
        if (msg.isEmpty()) return;

        try {
            String encryptedMessage = localUserEncryptedSession.encrypt(msg);
            String id = UUID.randomUUID().toString();
            // Get the current timestamp
            long timestamp = System.currentTimeMillis();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", id);
            hashMap.put("sender", fUser.getUid());
            hashMap.put("receiver", userIdContacted);
            hashMap.put("message", encryptedMessage);
            hashMap.put("isSeen", false);
            hashMap.put("timestamp", timestamp); // Add the timestamp to the message

            reference.child("SecretChats").push().setValue(hashMap);

            Chat myChat = new Chat(id, fUser.getUid(), userIdContacted, msg, false,timestamp);
            myChat.setTimestamp(timestamp);
            localChatsList.add(myChat);

            // Update the local chat list and save it to shared preferences
            Gson gson = new Gson();
            String chatSecretDeviceStorageJson = gson.toJson(localChatsList);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("chatSecretDeviceStorage", chatSecretDeviceStorageJson);
            editor.apply();
        } catch (InvalidMessageException e) {
            throw new RuntimeException(e);
        } catch (UntrustedIdentityException | InvalidKeyException | InvalidVersionException e) {
            e.printStackTrace();
        }
    }


    private void readNewMessage(Chat remoteMessage) {

        try {
            String encryptedMessage = remoteMessage.getMessage();
            String decryptedMessage = localUserEncryptedSession.decrypt(encryptedMessage);
            Log.d("myTag" , "readNewMessage  : " + decryptedMessage);

            remoteMessage.setMessage(decryptedMessage);
            remoteMessage.setSeen(true);
            localChatsList.add(remoteMessage);
            seenMessage(userIdContacted);

        } catch (UntrustedIdentityException | InvalidKeyException | InvalidMessageException | InvalidVersionException |
                 DuplicateMessageException | InvalidKeyIdException | LegacyMessageException e) {

            Log.d("myTag" ,  "FAILED DECRYPTION ON CATCH BLOCK ");

            e.printStackTrace();

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        isSecretMessageActivityResumed = true;
        checkStatus("online");
    //    seenMessage(userIdContacted); // Update the seen status only when the activity is resumed

    }

    @Override
    protected void onPause() {
        super.onPause();
        isSecretMessageActivityResumed = false;
        myReference.removeEventListener(seenListener);
        checkStatus("Offline");

        Gson gson = new Gson();
        String chatSecretDeviceStorageJson = gson.toJson(localChatsList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("chatSecretDeviceStorage", chatSecretDeviceStorageJson);
        editor.apply();
    }


}

