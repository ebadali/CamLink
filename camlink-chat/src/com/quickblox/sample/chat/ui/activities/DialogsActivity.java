package com.quickblox.sample.chat.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenFile;
import com.kbeanie.imagechooser.api.FileChooserListener;
import com.kbeanie.imagechooser.api.FileChooserManager;
import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.io.IOUtils;
import com.quickblox.sample.chat.ApplicationSingleton;
import com.quickblox.sample.chat.core.ChatService;
import com.quickblox.sample.chat.pushnotifications.Consts;
import com.quickblox.sample.chat.pushnotifications.PlayServicesHelper;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapters.DialogsAdapter;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.LoginActivity;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.quickblox.sample.chat.ui.activities.NewDialogActivity.getQBPagedRequestBuilder;

/**
 * Created by Ebad on 11/3/2015.
 */
public class DialogsActivity extends BaseActivity implements FileChooserListener{

    private static final String TAG = DialogsActivity.class.getSimpleName();

    private ListView dialogsListView;
    private ProgressBar progressBar;

    private PlayServicesHelper playServicesHelper;
    ArrayList<QBDialog> dialogs;
    private Context cntx;
    FileChooserManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs_activity);
        cntx = this;
        playServicesHelper = new PlayServicesHelper(this);

        dialogsListView = (ListView) findViewById(R.id.roomsList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

        // Get dialogs if the session is active
        //
        if (isSessionActive()) {
            getDialogs();
        }

    }

    private void getDialogs() {
        progressBar.setVisibility(View.VISIBLE);


        // Get dialogs
        //
        ChatService.getInstance().getDialogs(new QBEntityCallbackImpl() {
            @Override
            public void onSuccess(Object object, Bundle bundle) {
                progressBar.setVisibility(View.GONE);

                dialogs = (ArrayList<QBDialog>) object;

                // build list view
                //
                buildListView();
            }

            @Override
            public void onError(List errors) {
                progressBar.setVisibility(View.GONE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(DialogsActivity.this);
                dialog.setMessage("get dialogs errors: " + errors).create().show();
            }
        });
    }


    void buildListView() {
        final DialogsAdapter adapter = new DialogsAdapter(this.dialogs, DialogsActivity.this);
        dialogsListView.setAdapter(adapter);

        // choose dialog
        //

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) adapter.getItem(position);

                Bundle bundle = new Bundle();
                bundle.putSerializable(ChatActivity.EXTRA_DIALOG, selectedDialog);

                // Open chat activity
                //
                ChatActivity.start(DialogsActivity.this, bundle);

                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        playServicesHelper.checkPlayServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rooms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {

            // go to New Dialog activity
            //
            Intent intent = new Intent(DialogsActivity.this, NewDialogActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_call) {
            Call(true);
            return true;
        } else if (id == R.id.action_changepic) {
            ChangePic(true);
            return true;
        }
        else if (id == R.id.action_logout) {
            showLogOutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ChangePic(boolean b) {

        fm = new FileChooserManager(this);
        fm.setFileChooserListener(this);
        try {
            fm.choose();
        } catch (ChooserException e) {
            e.printStackTrace();
        }

    }

    private void showLogOutDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle(com.quickblox.sample.videochatwebrtcnew.R.string.log_out_dialog_title);
        quitDialog.setMessage(com.quickblox.sample.videochatwebrtcnew.R.string.log_out_dialog_message);

        quitDialog.setPositiveButton(com.quickblox.sample.videochatwebrtcnew.R.string.positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OpponentsAdapter.i = 0;
                QBChatService.getInstance().logout(new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {

                        finish();
                    }

                    @Override
                    public void onSuccess() {
                        finish();
                    }

                    @Override
                    public void onError(List list) {

                    }
                });

            }
        });

        quitDialog.setNegativeButton(com.quickblox.sample.videochatwebrtcnew.R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.show();
    }

    public ProgressDialog GetLoadingDialog(Context cntx) {
        ProgressDialog progressDoalog = new ProgressDialog(cntx);
        if (!progressDoalog.isShowing()) {
            progressDoalog.show();
        }
        //progressDoalog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        progressDoalog.setCancelable(false);
        progressDoalog.getWindow().setGravity(Gravity.CENTER);
        progressDoalog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDoalog.setContentView(R.layout.progressdialog);

        return progressDoalog;
    }

    ProgressDialog progressDialog;
    public void FileUploader(File file, Boolean fileIsPublic, String params) {


        QBRequestCanceler requestCanceler = QBContent.uploadFileTask(file, fileIsPublic, params, new QBEntityCallbackImpl<QBFile>() {
            @Override
            public void onSuccess(final QBFile qbFile, Bundle params) {
                // File public url. Will be null if fileIsPublic=false in query

                Log.i(TAG, "File Uploaded Succes : " + qbFile.getName());
                QBChatService.getInstance().getUser().setFileId(qbFile.getId());
                QBChatService.getInstance().getUser().setCustomData(qbFile.getPublicUrl());
                String password = QBChatService.getInstance().getUser().getPassword();
                QBChatService.getInstance().getUser().setOldPassword(password);
                QBChatService.getInstance().getUser().setPassword(password);

                QBUsers.updateUser(QBChatService.getInstance().getUser(), new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(final QBUser qbUser, Bundle bundle) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(cntx, "Image Updated", Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onError(List<String> strings) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(cntx, "Image Updation Failed", Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                });


            }

            @Override
            public void onError(final List<String> errors) {
                for (int i = 0; i < errors.size(); i++) {
                    Log.i(TAG, "Error : " + errors.get(i).toString());
                }



                runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(cntx, "File Upload Errors " + errors.toString(), Toast.LENGTH_LONG).show();
                    }
                });


            }
        });



    }

    private void Call(boolean isAudioOnly) {
        QBRTCTypes.QBConferenceType qbConferenceType = null;
        if (isAudioOnly) {
            qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        } else {
            qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;

        }
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("any_custom_data", "some data");
        userInfo.put("my_avatar_url", "avatar_reference");

        Log.d(TAG, "QBChatService.getInstance().isLoggedIn() = " + String.valueOf(QBChatService.getInstance().isLoggedIn()));

        com.quickblox.sample.videochatwebrtcnew.definitions.Consts.USER_LOGIN = ApplicationSingleton.USER_LOGIN; //ChatService.getInstance().getCurrentUser().getLogin();
        com.quickblox.sample.videochatwebrtcnew.definitions.Consts.USER_PASSWORD = ApplicationSingleton.USER_PASSWORD;//.getInstance().getCurrentUser().getPassword();
        com.quickblox.sample.videochatwebrtcnew.definitions.Consts.USER_ID = ChatService.getInstance().getCurrentUser().getFullName();
        com.quickblox.sample.videochatwebrtcnew.definitions.Consts.APP_SESSION = ApplicationSingleton.getInstance().APP_SESSION;

        com.quickblox.sample.videochatwebrtcnew.definitions.Consts.APP_ID = ApplicationSingleton.APP_ID;
        com.quickblox.sample.videochatwebrtcnew.definitions.Consts.AUTH_KEY = ApplicationSingleton.AUTH_KEY;
        com.quickblox.sample.videochatwebrtcnew.definitions.Consts.AUTH_SECRET = ApplicationSingleton.AUTH_SECRET;
//        DataHolder.usersList = qbUsers;

        startActivityForResult(new Intent(getBaseContext(), LoginActivity.class), 0);
//        QBUsers.getUsers(getQBPagedRequestBuilder(1,40), new QBEntityCallback<ArrayList<QBUser>>() {
//            @Override
//            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
//                Log.i(TAG,"getQBPagedRequestBuilder :onSuccess");
//
//
//
//            }
//
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onError(List<String> list) {
//                Log.i(TAG,"getQBPagedRequestBuilder :onError " + list.toString());
//            }
//        });
//        DataHolder.usersList = ChatService.getInstance().getDialogsUsers()


//        CallActivity.start(this, qbConferenceType, getOpponentsId(),
//                userInfo, Consts.CALL_DIRECTION_TYPE.OUTGOING);
    }

    private ArrayList<QBUser> GetUserList() {


        Map<Integer, QBUser> map = ChatService.getInstance().getDialogsUsers();
        ArrayList<QBUser> userList = new ArrayList<>(this.dialogs.size());
        for (QBUser user : map.values()) {
            userList.add(user);
        }
        return userList;
    }

    private List<Integer> getOpponentsId() {
        return null;
    }

    // Our handler for received Intents.
    //
    public static BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_MESSAGE);

            Log.i(TAG, "-----Receiving event " + Consts.NEW_PUSH_EVENT + " with data: " + message);
        }
    };


    //
    // ApplicationSessionStateCallback
    //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChooserType.REQUEST_PICK_FILE && resultCode == RESULT_OK) {
            if (fm == null) {
                fm = new FileChooserManager(this);
                fm.setFileChooserListener(this);
            }
            Log.i(TAG, "Probable file size: " + fm.queryProbableFileSize(data.getData(), this));
            fm.submit(requestCode, data);
        }
    }

    @Override
    public void onStartSessionRecreation() {

    }

    @Override
    public void onFinishSessionRecreation(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    getDialogs();
                }
            }
        });
    }

    @Override
    public void onFileChosen(ChosenFile chosenFile) {


//        if (progressDialog == null)
//            progressDialog = GetLoadingDialog(cntx);

        if (chosenFile.getFileName().contains(".png") || chosenFile.getFileName().contains(".jpg")) {
            Boolean flag = true;


            //  progressDialog.show();
            final File file = new File(chosenFile.getFilePath());
            final Map<String, String> data = new HashMap<String, String>();
            data.put("data[Size]", String.valueOf(file.length() / 1024));
            data.put("Size", String.valueOf(file.length() / 1024));
            data.put("size", String.valueOf(file.length() / 1024));


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = GetLoadingDialog(cntx);
                    progressDialog.show();

                    FileUploader(file,true,data.toString());
                }
            });

        }
        else
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(cntx,"Invalid File extension",Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onError(String s) {

    }

}
