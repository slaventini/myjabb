package com.myjabb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

public class MyJabbService extends Service {

    private XMPPTCPConnection mConnection;

    public MyJabbService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MyJabbApp getApp(){
        return (MyJabbApp)getApplication();
    }
}
