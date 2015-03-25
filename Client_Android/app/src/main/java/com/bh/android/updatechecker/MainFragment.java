package com.bh.android.updatechecker;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {


    private final boolean DBG = Config.DBG;

    private final String TAG = "BH_" + this.getClass().getSimpleName();

    public MainFragment() {
    }


    private ProgressDialog mProgressDialog = null;
    private void showProgressDialog(boolean isEnabled) {

        if(isEnabled == true) {

            if(mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext);
            }

            mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
            mProgressDialog.setMessage("Checking ...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();

        } else {
            if(mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    // @BH_Lin 20150323
    // retrun -1 = target version is less than current
    //        0 = target version is equal to current
    //        1 = target version is greater to current


    private final int TARGET_VERSION_NEWER = 1;
    private final int TARGET_VERSION_OLDER = -1;
    private final int TARGET_VERSION_EQUAL = 0;
    private int compareSelfVersion(String targetVer) {

        String currentVer = MyUtils.getSoftwareVersion(mContext);

        String[] currentVers = currentVer.split("\\.");
        String[] targetVers = targetVer.split("\\.");

        for(int i=0; i< 3; i++) {

            if(DBG)Log.d(TAG, "compare: [" + i + "]: " + currentVers[i] + " <>" + targetVers[i]);

            int currentVerIndexNumber = Integer.parseInt(currentVers[i]);
            int targetVerIndexNumber = Integer.parseInt(targetVers[i]);

            if( currentVerIndexNumber > targetVerIndexNumber) {

                return TARGET_VERSION_OLDER;

            } else if( currentVerIndexNumber < targetVerIndexNumber) {

                return TARGET_VERSION_NEWER;

            }
        }

        return TARGET_VERSION_EQUAL;

    }

    private BroadcastReceiver completeDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if(DBG)Log.d(TAG, "downloadId: " + downloadId + "," + completeDownloadId);

            if(completeDownloadId == downloadId) {

                Intent installIntent = new Intent(Intent.ACTION_VIEW);

                Uri fileUri = Uri.fromFile(new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        "/app.apk"));


                if(DBG)Log.d(TAG, "ready to install : " + fileUri);

                installIntent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(installIntent);

            }

        }
    };


    private long downloadId = -1;

    // @BH_Lin
    private void checkAvailableUpdate(String url) {

        DataRequester dataRequester = new DataRequester(mContext);
        if(DBG)Log.d(TAG, "@Server<<CheckLatestVersion");

        //showProgressDialog(true);

        dataRequester.httpGet(url, null, new DataRequester.CallBack() {
            @Override
            public void onCallback(String result) {

                if(DBG)Log.d(TAG, "@Server>>CheckLatestVersion:" + result);

                //showProgressDialog(false);

                if(ErrorHandle.mappingErrorCode(result) == ErrorHandle.ERR_SUCCESS) {

                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(result);
                        String latestVer = obj.getString("data");

                        Toast.makeText(mContext, "get latest version: " + latestVer, Toast
                                .LENGTH_SHORT).show();


                        if(compareSelfVersion(latestVer) == TARGET_VERSION_NEWER) {

                            new AlertDialog.Builder(mContext)
                                    .setMessage("A new version of app is available, do you want " +
                                            "to downlad it to update?")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {


                                            DownloadManager downloadManager = (DownloadManager)
                                                    mContext.getSystemService(Context.DOWNLOAD_SERVICE);

                                            DownloadManager.Request request = new DownloadManager
                                                    .Request(Uri.parse(Config.URL_DOWNLOAD_APP));
                                            request.setDestinationInExternalPublicDir("download",
                                                    "App.apk");
                                             request.setTitle("app.apk");
                                             request.setDescription("Please wait for downloading " +
                                                     "complete");
                                             request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            downloadId = downloadManager.enqueue(request);

                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(mContext)
                                .setMessage("parse object error in Server Responding data - check lastest" +
                                        " version")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                } else {
                    new AlertDialog.Builder(mContext)
                            .setMessage("There is an error occurred, please try again later or " +
                                    "contact the serice provider.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }

            }
        });

    }

    private Context mContext = null;


    private TextView mAppVerText = null;
    private EditText mServerUrlInput = null;

    private DataStore mDataStore = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mContext.registerReceiver(completeDownloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDataStore = new DataStore(mContext);

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mAppVerText = (TextView)view.findViewById(R.id.appVersion);
        mServerUrlInput = (EditText)view.findViewById(R.id.serverUrlInput);
        mServerUrlInput.setText(mDataStore.getServerDataUrl());

        Button btnSaveUrl = (Button)view.findViewById(R.id.btn_save_url);
        btnSaveUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newUrl = mServerUrlInput.getText().toString();
                mDataStore.setServerDataUrl(newUrl);

                new AlertDialog.Builder(mContext)
                        .setMessage("Saved new URL:\n" + newUrl)
                        .show();

            }
        });

        // @BH_Lin 20150323: button to check lastest version of app.
        Button button = (Button)view.findViewById(R.id.btn_check_update);
        if(button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(MyUtils.isNetworkAvailable(mContext) == true) {
                        myHandler.sendEmptyMessage(WM_CHECK_UPDATE);
                    } else {
                        new AlertDialog.Builder(mContext)
                                .setMessage("Network is not available, please check it and try " +
                                        "again later.")
                                .show();
                    }
                }
            });
        }


        return view;
    }


    private final int WM_CHECK_UPDATE = 10001;
    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            switch( msg.what) {
                case WM_CHECK_UPDATE:

                    checkAvailableUpdate(mDataStore.getServerDataUrl());

                    break;
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();

        mAppVerText.setText(MyUtils.getSoftwareVersion(mContext));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContext.unregisterReceiver(completeDownloadReceiver);
    }
}
