package com.bh.android.updatechecker;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by binghuan on 1/13/15.
 */
public class DataRequester {

    public final String TAG = "BH_" + this.getClass().getSimpleName();
    public static final boolean DBG = Config.DBG;

    public static final String USER_AGENT = "User-Agent";
    private DataStore mDataStore = null;
    private File tempFile = null;

    public Bitmap mBitmap2Upload = null;

    public JSONObject extraHeaders = null;

    interface CallBack {

        void onCallback(String result);

    }

    CallBack mCallback;

    private Context mContext = null;
    private JSONObject mInputData = null;
    private String resultString = null;

    public DataRequester(Context context) {
        mContext = context;
        mDataStore = new DataStore(context);
    }

    private String getResult() {
        return resultString;
    }

    public void httpPost(String url , JSONObject data, CallBack callback) {

        if(DBG)Log.v(TAG, "###> httpPost: " + url);
        if(data != null) {
            if(DBG)Log.v(TAG, "###> httpPostDATA: " + data.toString());
        }

        mInputData = data;
        mCallback = callback;
        new HttpAsyncTask().execute(url, String.valueOf(METHOD_POST));
    }

    public void httpPut(String url , JSONObject data, CallBack callback) {

        if(DBG)Log.v(TAG, "###> httpPut: " + url);

        mInputData = data;
        mCallback = callback;
        new HttpAsyncTask().execute(url, String.valueOf(METHOD_PUT));
    }

    public void httpDelete(String url , JSONObject data, CallBack callback) {

        if(DBG)Log.v(TAG, "###> httpDelete: " + url);

        mInputData = data;
        mCallback = callback;
        new HttpAsyncTask().execute(url, String.valueOf(METHOD_DELETE));
    }

    public void httpGet(String url , JSONObject data, CallBack callback) {

        if(DBG)Log.v(TAG, "###> httpGet: " + url);

        mInputData = data;
        mCallback = callback;
        new HttpAsyncTask().execute(url, String.valueOf(METHOD_GET));
    }

    public static final int METHOD_GET = 1;
    public static final int METHOD_PUT = 2;
    public static final int METHOD_POST = 3;
    public static final int METHOD_DELETE = 4;


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private String url = null;
        private int method = -1;

        @Override
        protected String doInBackground(String... params) {

            url = params[0];

            //Toast.makeText(mContext, "reqUrl: " + url, Toast.LENGTH_LONG).show();
            if(DBG)Log.v(TAG, "reqUrl: " + url);

            if(params[1] != null) {
                if(DBG)Log.v(TAG, "method: " + params[1]);
                method = Integer.parseInt(params[1]);
            }

            switch(method) {
                case METHOD_DELETE:
                    if(DBG)Log.v(TAG, "METHOD_DELETE");
                    return DELETE(url, mInputData);

                case METHOD_POST:
                    if(DBG)Log.v(TAG, "METHOD_POST");
                    return POST(url, mInputData);

                case METHOD_PUT:
                    if(DBG)Log.v(TAG, "METHOD_PUT");
                    return PUT(url, mInputData);

                case METHOD_GET:
                default:
                    if(DBG)Log.v(TAG, "METHOD_GET");
                    return GET(url, mInputData);
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            resultString = result;
            //if(DBG)Log.v(TAG, "###############> httpResponse: " + result);

            if(mCallback != null) {
                if(DBG)Log.d(TAG, "Ready to issue callback event");
                if( (tempFile != null) && tempFile.exists() == true ) {
                    tempFile.delete();
                    if(DBG)Log.d(TAG, "tempfile: " + tempFile.getAbsolutePath() + " has been " +
                            "deleted!");

                    /*
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(tempFile), "image/*");
                    mContext.startActivity(intent);
                    */
                }

                mCallback.onCallback(result);
            }
        }
    }


    private String GET(String url, JSONObject jsonObject) {

        if(DBG)Log.d(TAG, "#>> GET: " + url);

        InputStream inputStream = null;
        String result = "";
        try {

            // 2. make POST request to the given URL

            // BH_Lin@20150122 -------------------------------------------------------------------->
            // purpose: iterate jsonObject to append query string to URL

            String newUrl = url;

            // 4. convert JSONObject to JSON to String
            if(jsonObject != null) {

                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        String value = (String)jsonObject.get(key);

                        if(newUrl.toLowerCase().indexOf("?") == -1) {
                            newUrl += "?" + key + "=" + value;
                        } else {
                            newUrl += "&" + key + "=" + value;
                        }

                    } catch (JSONException e) {

                    }
                }

            }

            HttpGet httpGet = new HttpGet(newUrl);
            if(DBG)Log.d(TAG, "### query URL: " + newUrl);

            // 7. Set some headers to inform server about the type of the content
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");

            // BH_Lin@20150113  ------------------------------------------------------------------>

            if(extraHeaders != null) {

                Iterator<String> iterator = extraHeaders.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        String value = (String)extraHeaders.get(key);
                        httpGet.setHeader(key, value);

                        if(DBG)Log.d(TAG, "@### showHead: " + key + " = value ___" +  value +
                                "___");

                    } catch (JSONException e) {

                    }
                }
            }

            // BH_Lin@20150113  ------------------------------------------------------------------<

            // create HttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpClient.execute(httpGet);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private String DELETE(String url, JSONObject jsonObject) {
        if(DBG)Log.d(TAG, "#>> GET: " + url);

        InputStream inputStream = null;
        String result = "";
        try {

            // 2. make POST request to the given URL

            // BH_Lin@20150122 -------------------------------------------------------------------->
            // purpose: iterate jsonObject to append query string to URL

            String newUrl = url;

            // 4. convert JSONObject to JSON to String
            if(jsonObject != null) {

                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        String value = (String)jsonObject.get(key);

                        if(newUrl.toLowerCase().indexOf("?") == -1) {
                            newUrl += "?" + key + "=" + value;
                        } else {
                            newUrl += "&" + key + "=" + value;
                        }

                    } catch (JSONException e) {

                    }
                }

            }

            HttpDelete httpDelete = new HttpDelete(newUrl);
            if(DBG)Log.d(TAG, "### query URL: " + newUrl);

            // 7. Set some headers to inform server about the type of the content
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("Content-type", "application/json");

            // create HttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpClient.execute(httpDelete);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private String PUT(String url, JSONObject jsonObject) {

        if(DBG)Log.d(TAG, "#>> PUT: " + url);

        InputStream inputStream = null;
        String result = "";
        try {

            // 2. make POST request to the given URL
            HttpPut httpPut = new HttpPut(url);

            String json = "";

            StringEntity se = null;
            FileEntity fileEntity = null;
            // 4. convert JSONObject to JSON to String

            String contentUriString = "";

            if(jsonObject != null) {
                json = jsonObject.toString();

                Uri contentUri = null;
                Bitmap photo = null;

                if(mBitmap2Upload != null) {
                    if(DBG)Log.d(TAG, "Assign Bitmap data");
                    photo = mBitmap2Upload;
                } else {
                    contentUriString = jsonObject.getString("contentUri");
                    if(DBG)Log.d(TAG, "### get contentUri" + contentUriString);

                    if(contentUriString != null) {
                        contentUri = Uri.parse(contentUriString);
                        photo = MyUtils.getBitmap(mContext, contentUri);
                    }
                }

                // set Image via Data Uri

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 98, baos);
                byte[] byteArrayImage = baos.toByteArray();

                String newFileName = "SR" + MyUtils.getRandomString(10) + "photo.jpg";
                tempFile =new File(Environment.getExternalStorageDirectory(),newFileName);
                if(DBG)Log.d(TAG, "### temporary file: " + tempFile.getAbsolutePath());

                try {
                    FileOutputStream fos=new FileOutputStream(tempFile.getPath());

                    fos.write(byteArrayImage);
                    fos.close();
                }
                catch (java.io.IOException e) {
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }

                fileEntity = new FileEntity(tempFile, "/");

                if(DBG)Log.d(TAG, "### Set FileEntity !: " + byteArrayImage.length);
                httpPut.setEntity(fileEntity);
            } else {
                se = new StringEntity(json);
                if(DBG)Log.d(TAG, "### show sendingData: " + json);
                httpPut.setEntity(se);
            }

            // 7. Set some headers to inform server about the type of the content
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");

            // create HttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();


            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpClient.execute(httpPut);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private String POST(String url, JSONObject jsonObject){

        if(DBG)Log.d(TAG, "#>> POST: " + url);

        InputStream inputStream = null;
        String result = "";
        try {

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 4. convert JSONObject to JSON to String
            if(jsonObject != null) {
                json = jsonObject.toString();
            }

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);
            if(DBG)Log.d(TAG, "### show sendingData: " + json);

            // 6. set httpPost Entity

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            if(jsonObject != null) {

                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        Object value = jsonObject.get(key);
                        if(DBG)Log.e(TAG, "put key:" + key + ", value: " + value.toString());
                        if(value.toString().startsWith("[") && value.toString().endsWith("]")) {
                            if(DBG)Log.d(TAG,
                                    "PUT INTO Array!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            JSONArray array = new JSONArray(value.toString());
                            for(int i =0; i< array.length(); i++) {
                                nameValuePairs.add(new BasicNameValuePair(key + "[]",
                                        array.get(i).toString()));
                            }
                        } else {
                            nameValuePairs.add(new BasicNameValuePair(key, value.toString()));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "something wrong.");
                    }
                }

                if(DBG)Log.d(TAG, "### show sendingData: " + nameValuePairs.toString());
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "*/*");
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

            if(extraHeaders != null) {

                Iterator<String> iterator = extraHeaders.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        String value = (String)extraHeaders.get(key);
                        httpPost.setHeader(key, value);

                        if(DBG)Log.d(TAG, "@### showHead: " + key + " = value ___" +  value +
                                "___");

                    } catch (JSONException e) {

                    }
                }
            }

            // BH_Lin@20150113  ------------------------------------------------------------------<

            // create HttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        inputStream.close();
        return result;
    }
}
