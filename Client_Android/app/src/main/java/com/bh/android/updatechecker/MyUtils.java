package com.bh.android.updatechecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created by BH_Lin on 1/13/15.
 */
public class MyUtils {

    public final static String TAG = "BH_MyUtils";
    public static final boolean DBG = Config.DBG;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Activity
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static JSONObject getJsonObject(String... params) {
        JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < params.length; i++) {
            try {
                jsonObject.put(params[i], params[i + 1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            i++;
        }

        return jsonObject;
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getMd5String(String s) {

        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest .getInstance("MD5"); digest.update(s.getBytes()); byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getTimestamp() {
        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:ss.SSS");
        dateFormat.setTimeZone(cal.getTimeZone());
        //Log.d(TAG, "!(^_^)b >>  " + dateFormat.format(cal.getTime()) + "___");

        return dateFormat.format(cal.getTime());
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap getBitmap(Context context, Uri dataUri) {

        Bitmap photo = null;
        InputStream imageStream = null;
        try {
            imageStream = context.getContentResolver().openInputStream(dataUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Error in getBitmap:" + e.toString());
            e.printStackTrace();
        }

        if (imageStream != null) {
            photo = BitmapFactory.decodeStream(imageStream);
        }

        return photo;
    }

    public static String getRandomString(final int sizeOfRandomString) {
        String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";

        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public static AlertDialog showMsgDialog(Context context, final int iconResId, String title,
                                            String message, final Runnable onDismissed) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .create();

        if(title != null) {
            dialog.setTitle(title);
        }

        if(message != null) {
            dialog.setMessage(message);
        }

        if(iconResId != -1) {
            dialog.setIcon(iconResId);
        }

        dialog.show();

        return dialog;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static HashMap<String, String> getExtractedKeyMap(String jsonString) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            MyUtils.extractObjKey(jsonObject, map);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return map;
    }


    public static HashMap<String, String> getExtractedKeyMap(JSONObject jsonObject) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            MyUtils.extractObjKey(jsonObject, map);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return map;
    }

    public static Map<String,String> extractObjKey(JSONObject json , Map<String,
            String> out) throws JSONException{
        Iterator<String> keys = json.keys();
        while(keys.hasNext()){
            String key = keys.next();
            String val = null;
            try{
                JSONObject value = json.getJSONObject(key);
                extractObjKey(value,out);
            }catch(Exception e){
                val = json.getString(key);
            }

            if(val != null){
                out.put(key,val);
            }
        }
        return out;
    }


    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public static int countTrueInBooleanArray(boolean[] booleans) {

        int count = 0;

        for(int i=0; i< booleans.length; i++) {
            if(booleans[i] == true) {
                count +=1;
            }
        }

        if(DBG)Log.d(TAG,">> countTrueInBooleanArray: " + count);

        return count;
    }

    public static boolean isValueEmptyOrZero(String value) {

        boolean result = false;

        if( value == null ||
                (value.equals("") == true) ||
                (value.equals("0") == true)) {
            result = true;
        } else {
            result = false;
        }

        return result;
    }

    public static JSONObject pushKeys(JSONObject obj1, JSONObject obj2) {

        Iterator<String> iterator = obj2.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                obj1.put(key, obj2.get(key));

            } catch (JSONException e) {
                Log.e(TAG, "Error in pushKeys _checkpoint 98");
            }
        }

        if(DBG)Log.d(TAG, "pushKeys : " + obj1.toString());

        return obj1;
    }

    public static final String DENSITY_LDPI = "LDPI";
    public static final String DENSITY_MDPI = "MDPI";
    public static final String DENSITY_HDPI = "HDPI";
    public static final String DENSITY_XHDPI = "XHDPI";
    public static final String DENSITY_XXHDPI = "XXHDPI";
    public static float getDensity(Context context) {
        float  d = context.getResources().getDisplayMetrics().density;
        String result = DENSITY_HDPI;

        if(d == 0.75) {
            result = "LDPI";

        } else if (d == 1.0) {
            result = "MDPI";

        } else if (d == 1.5) {
            result = "HDPI";

        } else if (d == 2.0) {
            result = "XHDPI";

        } else if (d == 2.5) {
            result = "XXHDPI";
        }

        if(DBG)Log.d(TAG, "getDensity : " + d + " - " + result);
        return d;
    }

    public static void printHashKey(Context pContext) {

        PackageManager pkgMgr = pContext.getPackageManager();

        try {
            PackageInfo info = pkgMgr.getPackageInfo(pContext.getPackageName(),PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                if(DBG)Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }

    public static void playCashRegisterSfx(Context context) {
        return;
        /*
        MediaPlayer mp = MediaPlayer.create(context, R.raw.cash);
        mp.start();
        */
    }

    public static String getCustomizedUserAgent(Context context) {
        String  deviceSpString = "SweetRing/" + getSoftwareVersion(context) + " ";
        deviceSpString += "(Android " + Build.VERSION.RELEASE;
        deviceSpString+= "; " + context.getResources().getConfiguration().locale.toString();
        deviceSpString+= "; " + Build.MODEL + " Build/" + Build.ID;
        deviceSpString+= ")";

        if(DBG)Log.i(TAG, "User-Agent: " + deviceSpString);

        return deviceSpString;
    }

    public static String getSoftwareVersion(Context context) {
        String appVersion = "1.00";
        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if(DBG)Log.v(TAG, packageInfo.versionName);
            appVersion= packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        };


        if(DBG)Log.i(TAG, "<< getSoftwareVersion: " + appVersion);

        return appVersion;
    }


}
