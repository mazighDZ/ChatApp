package com.section41.whatsapp.model;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPrivateKey;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.StorageProtos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.section41.whatsapp.helper.Helper;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPrivateKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.util.ByteUtil;
import org.whispersystems.libsignal.util.KeyHelper;

import com.google.protobuf.ByteString;
import java.lang.reflect.Type;

public class User {
    private String id;
    private String username;
    private String imageURL;
    private String status;

    // Signal Protocol fields
    private String identityKeyPair;
    private int registrationId;
    private String preKeys;

    private String signedPreKeyRecord;
    private SignedPreKeyRecord signedPreKeyRecordObject;


    public User() {
    }

    public User(String id, String username, String imageURL, String status,
                String IdentityKeyPair, int registrationId,
                String preKeys, String signedPreKeyRecord) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.identityKeyPair = IdentityKeyPair;
        this.registrationId = registrationId;
        this.preKeys = preKeys;
        this.signedPreKeyRecord = signedPreKeyRecord;
    }

    public User(String id, String username, String imageURL, String status) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
    }

    // Getters and setters for all fields

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdentityKeyPairString() {
        return identityKeyPair;
    }

    public void setIdentityKeyPair(String identityKeyPair) {
        this.identityKeyPair = identityKeyPair;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getPreKeys() {
        return preKeys;
    }

    public void setPreKeys(String preKeys) {
        this.preKeys = preKeys;
    }

    public String getSignedPreKeyRecord() {
        return signedPreKeyRecord;
    }

    public void setSignedPreKeyRecord(String signedPreKeyRecord) {
        this.signedPreKeyRecord = signedPreKeyRecord;
    }

    // Other methods

    public List<PreKeyRecord> getPreKeysList() {
        // Convert the preKeys string to a list of PreKeyRecord objects
        Gson gson = new Gson();
        Type preKeysType = new TypeToken<List<PreKeyRecord>>() {}.getType();
        return gson.fromJson(preKeys, preKeysType);
    }

    public IdentityKeyPair getIdentityKeyPair() {
        try {

            byte[] identityKeyPairByte = Helper.decodeToByteArray(identityKeyPair);
            // Create the IdentityKeyPair using the IdentityKey and ECPrivateKey
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(identityKeyPairByte);

            Log.d("myTag", "IdentityKeyPair created successfully");
            return identityKeyPair;
        } catch (InvalidKeyException e) {
            Log.e("myTag", "Failed to create IdentityKeyPair", e);
            return null;
        }
    }



//    public IdentityKeyPair getIdentityKeyPair() {
//        try {
//            JSONObject identityKeyPairJson = new JSONObject(identityKeyPair);
//            JSONObject privateKeyJson = identityKeyPairJson.getJSONObject("privateKey");
//            JSONArray privateKeyBytesJson = privateKeyJson.getJSONArray("privateKey");
//            byte[] privateKeyBytes = Helper.decodeToByteArray(privateKeyBytesJson);
//
//            // publicKey
//            JSONObject publicKeyJson = identityKeyPairJson.getJSONObject("publicKey");
//            JSONObject publicKeyJson2 = publicKeyJson.getJSONObject("publicKey");
//            JSONArray publicKeyBytesJson = publicKeyJson2.getJSONArray("publicKey");
//            byte[] publicKeyBytes = Helper.decodeToByteArray(publicKeyBytesJson);
//
//            if (publicKeyBytes.length == 32) {
//                // The public key is missing the type specifier, probably from iOS
//                // Signal-Desktop handles this by ignoring the sent public key and regenerating it from the private key
//                byte[] type = {Curve.DJB_TYPE};
//                publicKeyBytes = ByteUtil.combine(type, publicKeyBytes);
//            }
//
//            // Create the ECPrivateKey and IdentityKey objects
//            ECPrivateKey privateKey = Curve.decodePrivatePoint(privateKeyBytes);
//            ECPublicKey publicKey = Curve.decodePoint(publicKeyBytes, 0);
//
//            // Create the IdentityKeyPair using the IdentityKey and ECPrivateKey
//            IdentityKeyPair identityKeyPair = new IdentityKeyPair(new IdentityKey(publicKey), privateKey);
//
//            Log.d("myTag", "IdentityKeyPair created successfully");
//            return identityKeyPair;
//        } catch (JSONException | InvalidKeyException e) {
//            Log.e("myTag", "Failed to create IdentityKeyPair", e);
//            return null;
//        }
//    }








    public List<PreKeyRecord> getPreKeysListAsList() {
        try {
            JSONArray preKeysArray = new JSONArray(preKeys);
            List<PreKeyRecord> preKeyRecords = new ArrayList<>();

            for (int i = 0; i < preKeysArray.length(); i++) {
                String preKeyBase64 = preKeysArray.getString(i);
                byte[] preKeyBytes = Helper.decodeToByteArray(preKeyBase64);
                PreKeyRecord preKeyRecord = new PreKeyRecord(preKeyBytes);
                preKeyRecords.add(preKeyRecord);
            }

            return preKeyRecords;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setSignedPreKeyRecordObject(SignedPreKeyRecord signedPreKeyRecord) {
        this.signedPreKeyRecordObject = signedPreKeyRecord;
    }

    public String[] getPreKeysArray() {
        // Split the preKeys string into individual preKeys
        String[] preKeysArray = preKeys.split(",");

        return preKeysArray;
    }

    public SignedPreKeyRecord getSignedPreKeyRecordObject() {
        try {

            byte[] signedPreKeyRecordBytes = Helper.decodeToByteArray(signedPreKeyRecord);

            SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyRecordBytes);
            return signedPreKeyRecord;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}
