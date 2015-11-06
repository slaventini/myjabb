package com.myjabb;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class MyJabbApp extends Application {

    private SharedPreferences mPreferences;

    private XMPPTCPConnection mConnection;
    private ChatMessageListener mMessListner;

    @Override
    public void onCreate() {
        super.onCreate();



    }

    public void addChatMessage(final String userid, final String mess) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {

                try {

                    List<ChatMess> list = loadChat(userid);

                    if (list == null){
                        list = new ArrayList<ChatMess>();
                    }

                    ChatMess chatMess = new ChatMess();
                    chatMess.message = mess;
                    chatMess.type = MessageType.Left;

                    list.add(chatMess);

                    Gson gson = new Gson();
                    String json = gson.toJson(list);
                    mPreferences.edit().putString(userid, json).commit();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }

    public void saveChat(final String userid, final List<ChatMess> list){

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {

                try {
                    Gson gson = new Gson();
                    String json = gson.toJson(list);
                    mPreferences.edit().putString(userid, json).commit();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }

    public List<ChatMess> loadChat(String userid){

        String json = mPreferences.getString(userid, "");

        if (json != null && !json.isEmpty()) {

            Gson gson = new Gson();
            Type listType = new TypeToken<List<ChatMess>>() {}.getType();
            List<ChatMess> list = gson.fromJson(json,listType);

            return list;
        }

        return null;

    }

    public XMPPTCPConnection getConnection(){
        return mConnection;
    }

    public ChatMessageListener getMessageListener(){
        return mMessListner;
    }

    public boolean login(String email, String passw, String host, String port){

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        //config.setUsernameAndPassword(mEmail, mPassword);
        config.setServiceName(host);
        config.setHost(host);
        config.setPort(Integer.valueOf(port));
        config.setDebuggerEnabled(true);

        mConnection = new XMPPTCPConnection(config.build());


        try {
            mConnection.connect();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mConnection.isConnected()) {

            try {
                SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
                //SASLAuthentication.blacklistSASLMechanism("PLAIN");
                mConnection.login(email, passw);
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mConnection.isAuthenticated()) {

                mPreferences = getSharedPreferences(Uri.parse(mConnection.getUser()).getPathSegments().get(0), Context.MODE_PRIVATE);

                mMessListner = new ChatMessageListener() {
                    @Override
                    public void processMessage(final Chat chat, final Message message) {

                        String userid = Uri.parse(message.getFrom()).getPathSegments().get(0);
                        addChatMessage(userid, message.getBody());

                        ((Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);

                        Intent intent = new Intent("MESSAGE");
                        intent.putExtra("message", message.getBody());
                        intent.putExtra("userid", userid);

                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    }
                };

                ChatManagerListener ManagerListner = new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally) {
                        if (!createdLocally)
                            chat.addMessageListener(mMessListner);
                    }
                };


                ChatManager chatmanager = ChatManager.getInstanceFor(getConnection());
                chatmanager.addChatListener(ManagerListner);

                return true;
            }
        }

        return false;
    }

}
