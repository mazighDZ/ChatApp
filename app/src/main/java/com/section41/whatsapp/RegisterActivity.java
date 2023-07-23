package com.section41.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.section41.whatsapp.helper.Helper;
import com.section41.whatsapp.registration.RegistrationKeyModel;
import com.section41.whatsapp.registration.RegistrationManager;


import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.libsignal.util.Medium;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    //widgets
    EditText userET ,emailET,passwordET;
    Button registerBtn;


    private FirebaseAuth mAuth ;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://whatsapp-781f2-default-rtdb.firebaseio.com/");
    DatabaseReference myRef ;
    IdentityKeyPair identityKeyPair;
    int registrationId ;
    List<PreKeyRecord> preKeys;
    SignedPreKeyRecord signedPreKeyRecord ;
    SharedPreferences sharedPreferences;
    RegistrationKeyModel registrationKeys;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //initialization
        userET = findViewById(R.id.edUsername);
        emailET = findViewById(R.id.edEmail);
        passwordET = findViewById(R.id.edPassword);
        registerBtn = findViewById(R.id.btnRegister);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Call the generateKeys() method to generate the keys

        //
         sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

/**
 * code for signla protocol keys
 * **/
        try {
             registrationKeys = generateKeys();
            // Use the generated keys as needed
            // For example, you can access individual keys like this:
             identityKeyPair = registrationKeys.getIdentityKeyPair();
             registrationId = registrationKeys.getRegistrationId();

             preKeys = registrationKeys.getPreKeys();
             signedPreKeyRecord = registrationKeys.getSignedPreKeyRecord();

            Log.d("mytag", identityKeyPair.getPrivateKey().getType()+ " "+identityKeyPair.getPublicKey().getPublicKey().getType() );
            //When we have successfully generated identity keys, registration id, and prekeys,
            // and signedPreKeyRecord we need to transmit the public part on our server & save it in our local
            // storage (Shared Preferences or Database).
            // TODO: Further processing with the generated keys


        } catch (InvalidKeyException | IOException e) {
            // Handle any exceptions that might occur during key generation
            e.printStackTrace();
        }

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(userET.getText().toString()) && !TextUtils.isEmpty(passwordET.getText().toString()) && !TextUtils.isEmpty(emailET.getText().toString())){

                    createUser(emailET.getText().toString().trim() , passwordET.getText().toString(),userET.getText().toString().trim(),  identityKeyPair,registrationId,  preKeys,   signedPreKeyRecord );
                }else {

                    Toast.makeText(getApplicationContext(),"Missing details" , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
           // reload();
        }
    }

    public void createUser(String email, String password, String username, IdentityKeyPair identityKeyPair, int registrationId, List<PreKeyRecord> preKeys, SignedPreKeyRecord signedPreKeyRecord) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(getApplicationContext(), "Register successfully", Toast.LENGTH_SHORT).show();
                FirebaseUser user = mAuth.getCurrentUser();
                String userId = user.getUid();
                myRef = database.getReference("Users").child(userId);

                HashMap<String, Object> myUserData = new HashMap<>();
                myUserData.put("id", userId);
                myUserData.put("username", username);
                myUserData.put("imageURL", "default");
                myUserData.put("status", "offline");

                myUserData.put("identityKeyPair", Helper.encodeToBase64( registrationKeys.getIdentityKeyPair().serialize())); // Convert identityKeyPair to JSON string
                myUserData.put("registrationId", registrationKeys.getRegistrationId());
                myUserData.put("preKeys", registrationKeys.getPreKeyIds()); // PreKeys first serialize then converted to String BASE64 then saved in lest then converted to Json file
                myUserData.put("signedPreKeyRecord", Helper.encodeToBase64(registrationKeys.signedPreKeyRecord()) ); // Convert signedPreKeyRecord to String Base64 and save in firebase
                // Save in shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("IdentityKeyPair", Helper.encodeToBase64( registrationKeys.getIdentityKeyPair().serialize())); // Convert identityKeyPair to JSON string
                editor.putInt("RegistrationId", registrationKeys.getRegistrationId());
                editor.putString("PreKeys",registrationKeys.getPreKeyIds()); // PreKeys first serialize then converted to String BASE64 then saved in lest then converted to Json file
                editor.putString("SignedPreKeyRecord", Helper.encodeToBase64(registrationKeys.signedPreKeyRecord())); // Convert signedPreKeyRecord to String Base64 and save in firebase
                editor.apply();

                myRef.setValue(myUserData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid password or Email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
    //signal protocol method

//    public static RegistrationKeyModel generateKeys() throws InvalidKeyException, IOException {
//        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
//
//        int registrationId = KeyHelper.generateRegistrationId(false);
//        SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair,new Random().nextInt(Medium.MAX_VALUE - 1));
//        List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(new Random().nextInt(Medium.MAX_VALUE - 101), 100);
//        return new RegistrationKeyModel(
//                identityKeyPair,
//                registrationId,
//                preKeys,
//                signedPreKey
//        );
//    }

    public static RegistrationKeyModel generateKeys() throws InvalidKeyException, IOException {

        RegistrationKeyModel registrationKeyModel  = RegistrationManager.generateKeys();

        return registrationKeyModel;
    }
}