package com.bh.android.updatechecker;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by binghuan on 1/26/15.
 */
public class ErrorHandle {

    public static final String TAG = "BH_SR_ErrorHandler";


    public static final int ERR_SUCCESS = 0;
    public static final int ERR_INCORRECT_FORMAT = -1;


    // BH_Lin@20150120  --------------------------------------------------------------------------->
    // purpose: mapping error cdoe
    public static int mappingErrorCode(String result) {

        JSONObject jsonObject = null;
        String mappingString = "";


        int errorCode = -1;

        try {
            jsonObject = new JSONObject(result);

            errorCode = jsonObject.getInt("return_code");
            switch(errorCode) {
                case ERR_SUCCESS:
                    mappingString = ":: Successful Request";
                    break;

                default:
                    mappingString = ":: An error occurred";
                    break;
            }

            Log.i(TAG, mappingString);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v(TAG, "JSON.parse --> FAIL");
            return ERR_INCORRECT_FORMAT;
        }

        return errorCode;

    }
    // BH_Lin@20150120  ---------------------------------------------------------------------------<
}
