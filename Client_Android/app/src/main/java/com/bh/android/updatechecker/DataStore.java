package com.bh.android.updatechecker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by binghuan on 1/16/15.
 */
public class DataStore {

    public final String TAG = "BH_" + this.getClass().getSimpleName();
    private final static boolean DBG = Config.DBG;

    private Context mContext = null;

    private final String KEY_SERVER_URL = "SERVER_DATA_URL";

    private SharedPreferences mSharedPreferences = null;
    private SharedPreferences.Editor mPrefEditor = null;


    public DataStore(Context context) {
        this.mContext = context;

        mContext = context;

        if(mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }

        if(mPrefEditor == null) {
            mPrefEditor = mSharedPreferences.edit();
        }
    }

    public void setServerDataUrl(String value) {
        mPrefEditor.putString(KEY_SERVER_URL, value).apply();

        if(DBG)Log.d(TAG, ">> setServerDataUrl: " + value);

    }

    public String getServerDataUrl() {

        String value = mSharedPreferences.getString(KEY_SERVER_URL, Config.URL_LATEST_APP_VER);
        if(DBG)Log.d(TAG, ">> setServerDataUrl: " + value);

        return value;
    }

}
