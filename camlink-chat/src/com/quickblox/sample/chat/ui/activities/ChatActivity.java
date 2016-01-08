package com.quickblox.sample.chat.ui.activities;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenFile;
import com.kbeanie.imagechooser.api.FileChooserListener;
import com.kbeanie.imagechooser.api.FileChooserManager;
import com.kbeanie.imagechooser.exceptions.ChooserException;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.io.IOUtils;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.core.Chat;
import com.quickblox.sample.chat.core.ChatService;
import com.quickblox.sample.chat.core.GroupChatImpl;
import com.quickblox.sample.chat.core.PrivateChatImpl;
import com.quickblox.sample.chat.ui.adapters.ChatAdapter;
import com.quickblox.sample.chat.utils.DialogUtil;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.ui.OnEmojiBackspaceClickListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.fragment.StickersFragment;
import vc908.stickerfactory.ui.view.KeyboardHandleRelativeLayout;

/**
 * Created by Ebad on 11/3/2015.
 */
public class ChatActivity extends BaseActivity implements KeyboardHandleRelativeLayout.KeyboardSizeChangeListener, FileChooserListener {

    public static final String EXTRA_DIALOG = "dialog";
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final String CTAG = "Custom Tag";
    public static String attachmentKey = "attachment";
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    public int CurrPosition;
    Context cntx;
    ProgressDialog progressDialog;
    FileChooserManager fm = null;
    private EditText messageEditText;
    private ListView messagesContainer;
    private Button sendButton;
    private ProgressBar progressBar;
    private ChatAdapter adapter;
    private Chat chat;
    private QBDialog dialog;
    ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.i(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection connection) {
            Log.i(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.i(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(final Exception e) {
            Log.i(TAG, "connectionClosedOnError: " + e.getLocalizedMessage());

            // leave active room
            //
            if (dialog.getType() == QBDialogType.GROUP) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((GroupChatImpl) chat).leave();
                    }
                });
            }
        }

        @Override
        public void reconnectingIn(final int seconds) {
            if (seconds % 5 == 0) {
                Log.i(TAG, "reconnectingIn: " + seconds);
            }
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "reconnectionSuccessful");

            // Join active room
            //
            if (dialog.getType() == QBDialogType.GROUP) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinGroupChat();
                    }
                });
            }
        }

        @Override
        public void reconnectionFailed(final Exception error) {
            Log.i(TAG, "reconnectionFailed: " + error.getLocalizedMessage());
        }
    };
    private KeyboardHandleRelativeLayout keyboardHandleLayout;
    private View stickersFrame;
    private boolean isStickersFrameVisible;
    private ImageView stickerButton;
    private RelativeLayout container;
    private OnStickerSelectedListener stickerSelectedListener = new OnStickerSelectedListener() {
        @Override
        public void onStickerSelected(String code) {
            if (StickersManager.isSticker(code)) {
                sendChatMessage(code, null);
//                setStickersFrameVisible(false);
            } else {
                // append emoji to edit
                messageEditText.append(code);
            }
        }
    };
    private View.OnClickListener Download = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Download Button
            if (adapter.getItem(CurrPosition).getBody().contains(attachmentKey)) {
                DownloadFile(adapter.getItem(CurrPosition));
            } else {
                Toast.makeText(cntx, "Cant Download Text", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private View.OnClickListener Delete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Delete Button


            DeleteThisChat(adapter.remove(CurrPosition));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    adapter.notifyDataSetChanged();
                }
            });

        }
    };

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }

    public static ProgressDialog GetLoadingDialog(Context cntx) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        this.cntx = this;
        // Init chat if the session is active
        //
        if (isSessionActive()) {
            initChat();
        }

        ChatService.getInstance().addConnectionListener(chatConnectionListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.call, menu);
        return true;
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.selectfile) {

            // go to New Dialog activity
            //
            fm = new FileChooserManager(this);
            fm.setFileChooserListener(this);
            try {
                fm.choose();
            } catch (ChooserException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ChatService.getInstance().removeConnectionListener(chatConnectionListener);
    }

    @Override
    public void onBackPressed() {
        if (isStickersFrameVisible) {
            setStickersFrameVisible(false);
            stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            try {
                chat.release();
            } catch (XMPPException e) {
                Log.e(TAG, "failed to release chat", e);
            }
            super.onBackPressed();

            Intent i = new Intent(ChatActivity.this, DialogsActivity.class);
            startActivity(i);
            finish();
        }
    }



    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView companionLabel = (TextView) findViewById(R.id.companionLabel);

        // Setup opponents info
        //
        Intent intent = getIntent();
        dialog = (QBDialog) intent.getSerializableExtra(EXTRA_DIALOG);
        container = (RelativeLayout) findViewById(R.id.container);
        if (dialog.getType() == QBDialogType.GROUP) {
            TextView meLabel = (TextView) findViewById(R.id.meLabel);
            container.removeView(meLabel);
            container.removeView(companionLabel);
        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);
             QBRoster chatRoster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, new QBSubscriptionListener() {
                @Override
                public void subscriptionRequested(int userID) {

                }
            });
            String online =  "";
            QBPresence presence = chatRoster.getPresence(opponentID);
            if (presence == null) {
                // No user in your roster
                return;
            }

            if (presence.getType() == QBPresence.Type.online) {
                // User is online
                online =  "Online: ";
            } else {
                // User is offline
                online =  "Offline: ";
            }
            companionLabel.setText(ChatService.getInstance().getDialogsUsers().get(opponentID).getLogin());
        }

        // Send button
        //
        sendButton = (Button) findViewById(R.id.chatSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                sendChatMessage(messageText, null);

            }
        });

        // Stickers
        keyboardHandleLayout = (KeyboardHandleRelativeLayout) findViewById(R.id.sizeNotifierLayout);
        keyboardHandleLayout.listener = this;
        stickersFrame = findViewById(R.id.frame);
        stickerButton = (ImageView) findViewById(R.id.stickers_button);

        stickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStickersFrameVisible) {
                    showKeyboard();
                    stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
                } else {
                    if (keyboardHandleLayout.isKeyboardVisible()) {
                        keyboardHandleLayout.hideKeyboard(ChatActivity.this, new KeyboardHandleRelativeLayout.OnKeyboardHideCallback() {
                            @Override
                            public void onKeyboardHide() {
                                stickerButton.setImageResource(R.drawable.ic_action_keyboard);
                                setStickersFrameVisible(true);
                            }
                        });
                    } else {
                        stickerButton.setImageResource(R.drawable.ic_action_keyboard);
                        setStickersFrameVisible(true);
                    }
                }
            }
        });

        updateStickersFrameParams();
        StickersFragment stickersFragment = (StickersFragment) getSupportFragmentManager().findFragmentById(R.id.frame);
        if (stickersFragment == null) {
            stickersFragment = new StickersFragment.Builder()
                    .setStickerPlaceholderColorFilterRes(android.R.color.darker_gray)
                    .build();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, stickersFragment).commit();
        }
        stickersFragment.setOnStickerSelectedListener(stickerSelectedListener);
        stickersFragment.setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked() {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                messageEditText.dispatchKeyEvent(event);
            }
        });
        setStickersFrameVisible(isStickersFrameVisible);
    }

    private void showKeyboard() {
        ((InputMethodManager) messageEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void sendChatMessage(String messageText, QBAttachment file) {
        QBChatMessage chatMessage = new QBChatMessage();

        chatMessage.setProperty("status","online");
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(new Date().getTime() / 1000);
        if (file != null) {

            chatMessage.addAttachment(file);
            messageText = attachmentKey;
        }

        chatMessage.setBody(messageText);
        try {
            chat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            Log.e(TAG, "failed to send a message", e);
        } catch (SmackException sme) {
            Log.e(TAG, "failed to send a message", sme);
        }

        messageEditText.setText("");

        if (dialog.getType() == QBDialogType.PRIVATE) {
            showMessage(chatMessage);
        }
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            setStickersFrameVisible(false);
            stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            if (isStickersFrameVisible) {
                stickerButton.setImageResource(R.drawable.ic_action_keyboard);
            } else {
                stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
            }
        }
    }

    private void setStickersFrameVisible(final boolean isVisible) {
        stickersFrame.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        isStickersFrameVisible = isVisible;
        if (stickersFrame.getHeight() != vc908.stickerfactory.utils.KeyboardUtils.getKeyboardHeight()) {
            updateStickersFrameParams();
        }
        final int padding = isVisible ? vc908.stickerfactory.utils.KeyboardUtils.getKeyboardHeight() : 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            keyboardHandleLayout.post(new Runnable() {
                @Override
                public void run() {
                    setContentBottomPadding(padding);
                    scrollDown();
                }
            });
        } else {
            setContentBottomPadding(padding);
        }
        scrollDown();
    }

    private void updateStickersFrameParams() {
        stickersFrame.getLayoutParams().height = vc908.stickerfactory.utils.KeyboardUtils.getKeyboardHeight();
    }

    public void setContentBottomPadding(int padding) {
        container.setPadding(0, 0, 0, padding);
    }

    private void initChat() {

        if (dialog.getType() == QBDialogType.GROUP) {
            chat = new GroupChatImpl(this);

            // Join group chat
            //
            progressBar.setVisibility(View.VISIBLE);
            //
            joinGroupChat();

        } else if (dialog.getType() == QBDialogType.PRIVATE) {

            Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);


            chat = new PrivateChatImpl(this, opponentID, dialog.getName());

            // Load CHat history
            //
            loadChatHistory();
        }
    }

    private void joinGroupChat() {
        ((GroupChatImpl) chat).joinGroupChat(dialog, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {

                // Load Chat history
                //
                loadChatHistory();
            }

            @Override
            public void onError(List list) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                dialog.setMessage("error when join group chat: " + list.toString()).create().show();
            }
        });
    }

    private void loadChatHistory() {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);
        customObjectRequestBuilder.sortDesc("date_sent");

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {

                adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBChatMessage>());
                messagesContainer.setAdapter(adapter);
                messagesContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                        CurrPosition = position;
                        ProgressDialog dialog = null;
                        dialog = DialogUtil.GetChatActions(cntx,
                                Download,
                                Delete);
                        dialog.show();
                    }

                });

                for (int i = messages.size() - 1; i >= 0; --i) {
                    QBChatMessage msg = messages.get(i);
                    showMessage(msg);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                if (!ChatActivity.this.isFinishing()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                    dialog.setMessage("load chat history errors: " + errors).create().show();
                }
            }
        });
    }

    private void DeleteThisChat(QBChatMessage item) {


        Set<String> messageIds = new TreeSet<>();
        messageIds.add(item.getId());
        QBChatService.deleteMessages(messageIds, new QBEntityCallbackImpl<Void>() {
            @Override
            public void onSuccess() {
                Toast.makeText(cntx, "Chat Message Deleted Succesfully", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(List<String> errors) {
                Toast.makeText(cntx, "Error Deleting Chat Message : " + errors.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createNotification(QBChatMessage message) {
        // Prepare intent which is triggered if the
        // notification is selected
        try {
            Intent intent = new Intent(this, ChatActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // Build notification
            // Actions are just fake
            Notification noti = new Notification.Builder(this)
                    .setContentTitle("Message: " +
                            ChatService.getInstance().getDialogsUsers().get(message.getSenderId()).getLogin())
                    .setContentText("" + message.getBody()).setSmallIcon(R.drawable.app_icon)
                    .setSound(alarmSound).build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);

        } catch (Exception ex) {
        }
    }

    public void showMessage(QBChatMessage message) {


        Log.i(CTAG, "new message " + message.getBody());
        adapter.add(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }


    //
    // ApplicationSessionStateCallback
    //

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
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
                    initChat();
                }
            }
        });
    }

    @Override
    public void onFileChosen(ChosenFile chosenFile) {
        Log.i(TAG, chosenFile.getFileName());
        final File file = new File(chosenFile.getFilePath());

        final Map<String, String> data = new HashMap<String, String>();
        data.put("data[Size]", String.valueOf(file.length() / 1024));
        data.put("Size", String.valueOf(file.length() / 1024));
        data.put("size", String.valueOf(file.length() / 1024));
        runOnUiThread(new Runnable() {
            public void run() {

                FileUploader(file, true, data.toString());
            }
        });
    }

    @Override
    public void onError(String s) {

    }

    public void FileUploader(File file, Boolean fileIsPublic, String params) {

        if (progressDialog == null)
            progressDialog = GetLoadingDialog(cntx);
        progressDialog.show();
        QBRequestCanceler requestCanceler = QBContent.uploadFileTask(file, fileIsPublic, params, new QBEntityCallbackImpl<QBFile>() {
            @Override
            public void onSuccess(final QBFile qbFile, Bundle params) {
                // File public url. Will be null if fileIsPublic=false in query

                Log.i(TAG, "File Uploaded Succes : " + qbFile.getName());

                QBAttachment attachment = new QBAttachment(qbFile.getContentType());
                attachment.setName(qbFile.getName());
                attachment.setId(qbFile.getId().toString());
                attachment.setSize((double) qbFile.getSize());
                attachment.setUrl(qbFile.getPublicUrl());

                sendChatMessage(attachmentKey, attachment);
                progressDialog.dismiss();
//                final List<QBAttachment> attch = new ArrayList<QBAttachment>();
//                attch.add(attachment);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(cntx, "File Uploaded " + qbFile.getPublicUrl(), Toast.LENGTH_LONG).show();

                    }
                });

            }

            @Override
            public void onError(final List<String> errors) {
                for (int i = 0; i < errors.size(); i++) {
                    Log.i(TAG, "File Error : " + errors.get(i).toString());
                }

                progressDialog.dismiss();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(cntx, "File Upload Errors " + errors.toString(), Toast.LENGTH_LONG).show();
                    }
                });


            }
        });

    }

    public void DownloadFile(QBChatMessage item) {


        item.getAttachments();
        int someId = -1;
        String fileName = "";
        for (QBAttachment attch : item.getAttachments()) {
            try {
                someId = Integer.parseInt(attch.getId());
                fileName = attch.getName();
            } catch (Exception ex) {
            }
            break;

        }
        // Download file with ID 126
        if (someId == -1)
            return;
        final String finalFileName = fileName;

        progressDialog = GetLoadingDialog(this);
        progressDialog.show();
        QBContent.downloadFileTask(someId, new QBEntityCallbackImpl<InputStream>() {
            @Override
            public void onSuccess(InputStream inputStream, Bundle params) {
                Log.i(TAG, "File Downloading");

                // Get the directory for the app's private pictures directory.

                String root = Environment.getExternalStorageDirectory().toString();// cntx.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root + "/saved_images");
                myDir.mkdirs();
                File file = new File(myDir, finalFileName);
                if (file.exists()) file.delete();

                new DownloadTask(file).execute(inputStream);


            }

            @Override
            public void onError(List<String> errors) {
                Log.i(TAG, "File Downloading Failed " + errors.toString());
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {

            }
        });
    }

    private int GetRandomNumber() {
        return new Random(10).nextInt();
    }

    class DownloadTask extends AsyncTask<InputStream, Void, Boolean> {

        File mFile;
        private Exception exception;

        public DownloadTask(File file) {
            this.mFile = file;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(InputStream... params) {
            Log.e(TAG, "Directory not created");
            Boolean flag = true;
            try {

                FileOutputStream outputStream = new FileOutputStream(mFile);
                IOUtils.copy(params[0], outputStream);
                params[0].close();
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
                flag = false;
            }
            return flag;
        }

        protected void onPostExecute(Boolean flag) {
            // TODO: check this.exception
            // TODO: do something with the feed
            //+ mFile.getAbsolutePath()
            final String status = flag ? "Completed "  : "Failed";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(cntx, "File Downloading " + status, Toast.LENGTH_LONG).show();
                }
            });
            progressDialog.dismiss();

        }

    }
}
