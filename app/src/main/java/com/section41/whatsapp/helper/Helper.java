package com.section41.whatsapp.helper;


import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;

public class Helper {
    public static String encodeToBase64(byte[] value) {
        return Base64.encodeToString(value, Base64.NO_WRAP);
    }

    public static byte[] decodeToByteArray(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }


    //converting jsonArray if you retrieve data from firebase as jsonArray format
    public static byte[] decodeToByteArray(JSONArray jsonArray) throws JSONException {
        byte[] byteArray = new byte[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            byteArray[i] = (byte) jsonArray.getInt(i);
        }

        return byteArray;
    }

}