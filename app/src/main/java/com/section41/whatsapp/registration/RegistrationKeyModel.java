package com.section41.whatsapp.registration;



import com.google.gson.Gson;
import com.section41.whatsapp.helper.Helper;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

    public class RegistrationKeyModel {
        IdentityKeyPair identityKeyPair;
        int registrationId;
        List<PreKeyRecord> preKeys;
        SignedPreKeyRecord signedPreKeyRecord;

        public RegistrationKeyModel(IdentityKeyPair identityKeyPair, int registrationId, List<PreKeyRecord> preKeys, SignedPreKeyRecord signedPreKeyRecord) {
            this.identityKeyPair = identityKeyPair;
            this.registrationId = registrationId;
            this.preKeys = preKeys;
            this.signedPreKeyRecord = signedPreKeyRecord;
        }

        public RegistrationKeyModel(String identityKeyPair, int registrationId, String[] preKeys, String signedPreKeyRecord) throws InvalidKeyException, IOException, IOException {
            this.identityKeyPair = new IdentityKeyPair(Helper.decodeToByteArray(identityKeyPair));
            this.registrationId = registrationId;
            List<PreKeyRecord> preKeyRecords = new ArrayList<>();
            for (String item : preKeys) {
                preKeyRecords.add(new PreKeyRecord(Helper.decodeToByteArray(item)));
            }
            this.preKeys = preKeyRecords;
            this.signedPreKeyRecord = new SignedPreKeyRecord(Helper.decodeToByteArray(signedPreKeyRecord));
        }

        public IdentityKeyPair getIdentityKeyPair() {
            return identityKeyPair;
        }



        public String getIdentityKeyPublicString() {
            return Helper.encodeToBase64(identityKeyPair.getPublicKey().serialize());
        }

        public void setIdentityKeyPair(IdentityKeyPair identityKey) {
            this.identityKeyPair = identityKey;
        }



        public void setRegistrationId(int registrationId) {
            this.registrationId = registrationId;
        }

        public List<PreKeyRecord> getPreKeys() {
            return preKeys;
        }

        public List<byte[]> getPreKeysBytes() {
            List<byte[]> result = new ArrayList<>();

            for(PreKeyRecord preKey : preKeys) {
                result.add(preKey.serialize());
            }

            return result;
        }

        public byte[] signedPreKeyRecord() {
            return signedPreKeyRecord.serialize();
        }

        public SignedPreKeyRecord getSignedPreKeyRecord() {
            return signedPreKeyRecord;
        }



        public void setSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
            this.signedPreKeyRecord = signedPreKeyRecord;
        }

        public String getPublicIdentityKey() {
            return Helper.encodeToBase64(identityKeyPair.getPublicKey().serialize());
        }

        public String getSignedPreKeyPublicKey() {
            return Helper.encodeToBase64(signedPreKeyRecord.getKeyPair().getPublicKey().serialize());
        }

        public int getSignedPreKeyId() {
            return signedPreKeyRecord.getId();
        }

        public String getSignedPreKeyRecordSignature() {
            return Helper.encodeToBase64(signedPreKeyRecord.getSignature());
        }

//those method to send into data base
        public String getIdentityKeyPairString() {
            return Helper.encodeToBase64(identityKeyPair.serialize());
        }

        public int getRegistrationId() {

            return registrationId;
        }
        public String getPreKeyIds() {
            List<String> preKeyList = new ArrayList<>();
            for (PreKeyRecord preKey : preKeys) {
                preKeyList.add(Helper.encodeToBase64(preKey.serialize()));
            }
            return new Gson().toJson(preKeyList);
        }

        public String getSignedPreKeyRecordString() {
            return Helper.encodeToBase64(signedPreKeyRecord.serialize());
        }
    }

