package com.myjabb;




import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;

import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.datatype.Duration;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private String userid;
    private static ListView lstMessages;
    private static MessagesArrayAdapter adapter;
    private List<ChatMess> msgList;
    private Chat newChat;
    public static final int GALLERY_FILE = 0;
    private FileTransferManager mFmanager;
    private FileTransferListener mFileTransferListener;

    class MessagesArrayAdapter extends ArrayAdapter<ChatMess> {

        public MessagesArrayAdapter(Context context, List<ChatMess> values) {
            super(context, R.layout.chat_list_item, values);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Tag tag;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_list_item, parent, false);
                convertView.setTag(tag = new Tag(convertView));
            } else {
                tag = (Tag) convertView.getTag();
            }

            final ChatMess mess = getItem(position);

            tag.right.setVisibility(View.GONE);
            tag.left.setVisibility(View.GONE);
            tag.left_img_ll.setVisibility(View.GONE);
            tag.left_img.setImageBitmap(null);
            tag.right_img_ll.setVisibility(View.GONE);
            tag.right_img.setImageBitmap(null);

            if (mess.type == MessageType.Left) {

                tag.left.setText(mess.message);
                tag.left.setVisibility(View.VISIBLE);

            } else if (mess.type == MessageType.Right) {
                tag.right.setText(mess.message);
                tag.right.setVisibility(View.VISIBLE);

            } else if (mess.type == MessageType.LeftImage) {


                tag.left_img_ll.setVisibility(View.VISIBLE);

                new AsyncTask<Tag, Void, Bitmap>(){

                    private Tag tag;

                    @Override
                    protected Bitmap doInBackground(Tag... tags) {

                        tag = tags[0];

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inSampleSize = 4;

                        Bitmap myBitmap = BitmapFactory.decodeFile(mess.message, options);
                        return myBitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap myBitmap) {
                        super.onPostExecute(myBitmap);
                        tag.left_img.setImageBitmap(myBitmap);
                        ;

                    }
                }.execute(tag);


            } else if (mess.type == MessageType.RighImage) {

                tag.right_img_ll.setVisibility(View.VISIBLE);

                new AsyncTask<Tag, Void, Bitmap>(){

                    private Tag tag;

                    @Override
                    protected Bitmap doInBackground(Tag... tags) {

                        tag = tags[0];

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inSampleSize = 4;

                        Bitmap myBitmap = BitmapFactory.decodeFile(mess.message,options);
                        return myBitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap myBitmap) {
                        super.onPostExecute(myBitmap);
                        tag.right_img.setImageBitmap(myBitmap);


                    }
                }.execute(tag);

            }

            return  convertView;
        }

        final class Tag {

            final TextView left;
            final TextView right;
            final View left_img_ll;
            final View right_img_ll;
            final ImageView left_img;
            final ImageView right_img;


            Tag(View view){

                left = (TextView) view.findViewById(R.id.left);
                right = (TextView) view.findViewById(R.id.right);

                left_img_ll = view.findViewById(R.id.left_img_ll);
                right_img_ll = view.findViewById(R.id.right_img_ll);

                left_img = (ImageView) view.findViewById(R.id.left_img);
                right_img = (ImageView) view.findViewById(R.id.right_img);
            }
        }


    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            String mess_userid = intent.getStringExtra("userid");

            if (userid != null && !userid.isEmpty() && userid.equalsIgnoreCase(mess_userid) ) {
                addChatMessage(message, MessageType.Left);
            }
        }
    };


    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("MESSAGE"));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mMessageReceiver);
        newChat.removeMessageListener(getApp().getMessageListener());
        userid = null;
        lstMessages = null;
        adapter = null;
        newChat = null;
        mMessageReceiver = null;

        mFmanager.removeFileTransferListener(mFileTransferListener);
        mFileTransferListener = null;
        mFmanager = null;

        msgList.clear();
        msgList = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        if (getArguments() != null) {

            userid = getArguments().getString("userid");

            if (userid != null && !userid.isEmpty()) {

                getActivity().setTitle(userid);

                List<ChatMess> arhMessages = getApp().loadChat(userid);

                if (arhMessages != null) {
                    msgList = arhMessages;
                } else {
                    msgList = new ArrayList<ChatMess>();
                }

                adapter = new MessagesArrayAdapter(this.getActivity(), msgList);

                lstMessages = (ListView) view.findViewById(R.id.msgList);
                lstMessages.setAdapter(adapter);

                if (adapter.getCount() > 1) {
                    lstMessages.setSelection(adapter.getCount() - 1);
                }

                ChatManager chatmanager = ChatManager.getInstanceFor(getApp().getConnection());

                newChat = chatmanager.createChat(userid);
                newChat.addMessageListener(getApp().getMessageListener());

                mFileTransferListener = new FileTransferListener() {
                    @Override
                    public void fileTransferRequest(final FileTransferRequest request) {


//                        IncomingFileTransfer transfer = request.accept();
//                        File mf = Environment.getExternalStorageDirectory();
//                        final File file = new File(mf.getAbsoluteFile()+"/DCIM/Camera/" + transfer.getFileName());
//                        try{
//                            transfer.recieveFile(file);
//                            while(!transfer.isDone()) {
//                                try{
//                                    Thread.sleep(1000L);
//                                }catch (Exception e) {
//                                    Log.e("", e.getMessage());
//                                }
//                                if(transfer.getStatus().equals(FileTransfer.Status.error)) {
//                                    Toast.makeText(getActivity(), "ОШИБКА" + transfer.getError(), Toast.LENGTH_SHORT).show();
//                                }
//                                if(transfer.getException() != null) {
//                                    transfer.getException().printStackTrace();
//                                }
//                            }
//
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    addChatMessage(file.getAbsolutePath(), MessageType.LeftImage);
//                                    getApp().saveChat(userid, msgList);
//                                }
//                            });
//
//
//
//                        }catch (Exception e) {
//                            Log.e("", e.getMessage());
//                        }

                        new Thread(){
                            @Override
                            public void run() {
                                IncomingFileTransfer transfer = request.accept();
                                File mf = Environment.getExternalStorageDirectory();
                                final File file = new File(mf.getAbsoluteFile()+"/DCIM/Camera/" + transfer.getFileName());
                                try{
                                    transfer.recieveFile(file);
                                    while(!transfer.isDone()) {
                                        try{
                                            Thread.sleep(1000L);
                                        }catch (Exception e) {
                                            Log.e("", e.getMessage());
                                        }
                                        if(transfer.getStatus().equals(FileTransfer.Status.error)) {
                                            Toast.makeText(getActivity(), "ОШИБКА" + transfer.getError(), Toast.LENGTH_SHORT).show();
                                        }
                                        if(transfer.getException() != null) {
                                            transfer.getException().printStackTrace();
                                        }
                                    }

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
                                            addChatMessage(file.getAbsolutePath(), MessageType.LeftImage);
                                            getApp().saveChat(userid, msgList);
                                        }
                                    });

                                }catch (Exception e) {
                                    Log.e("", e.getMessage());
                                }
                            };
                        }.start();
                    }
                };

                mFmanager = FileTransferManager.getInstanceFor(getApp().getConnection());
                mFmanager.addFileTransferListener(mFileTransferListener);

                final EditText edtMessage = (EditText)view.findViewById(R.id.message);
                Button butSend = (Button)view.findViewById(R.id.send);
                butSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String msg = edtMessage.getText().toString();

                        if (!msg.isEmpty()) {

                            try {
                                newChat.sendMessage(msg);

                                addChatMessage(msg, MessageType.Right);
                                edtMessage.setText("");

                                getApp().saveChat(userid, msgList);

                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                Button butSendImage = (Button)view.findViewById(R.id.add_img);
                butSendImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                            Toast.makeText(getActivity(), "Приложение \"Галерея\" не найдено", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        getRootFragment().startActivityForResult(intent, GALLERY_FILE);

                    }
                });
            }
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if ( requestCode == GALLERY_FILE) {
            Cursor cursor = getActivity().getContentResolver().query(data.getData(), new String[] { MediaStore.Images.Media.DATA }, null, null, null);
            if (cursor != null) {

                int idata = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                if (idata > -1 && cursor.moveToFirst()) {

                    addChatMessage(cursor.getString(idata), MessageType.RighImage);

                    OutgoingFileTransfer transfer = mFmanager.createOutgoingFileTransfer(userid+"/Smack");
                    File file = new File(cursor.getString(idata));

                    try {
                        transfer.sendFile(file, "image file");
                    } catch (SmackException e) {
                        e.printStackTrace();
                    }

                    while(!transfer.isDone()) {
                        if(transfer.getStatus().equals(FileTransfer.Status.error)) {
                            Toast.makeText(getActivity(), "ОШИБКА!" + transfer.getError(), Toast.LENGTH_SHORT).show();
                        } else if (transfer.getStatus().equals(FileTransfer.Status.cancelled)
                                || transfer.getStatus().equals(FileTransfer.Status.refused)) {
                            Toast.makeText(getActivity(), "ОТМЕНА!" + transfer.getError(), Toast.LENGTH_SHORT).show();
                        }
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    getApp().saveChat(userid, msgList);
                }
                cursor.close();
            }
        }
    }

    private Fragment getRootFragment() {
        Fragment root = this;
        do {
            Fragment parent = (Fragment) root.getParentFragment();
            if (parent == null) {
                return root;
            } else {
                root = parent;
            }
        } while (true);
    }

    private void addChatMessage(String message, MessageType type) {

        ChatMess mess = new ChatMess();
        mess.message = message;
        mess.type = type;

        adapter.add(mess);
        lstMessages.setSelection(adapter.getCount()-1);

    }

    public MyJabbApp getApp(){
        return ((ChatActivity)getActivity()).getApp();
    }

}
