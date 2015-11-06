package com.myjabb;


import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.logging.Log;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket;

import java.util.Collection;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatListFragment extends Fragment {

    private ListView mList;

    class ChatArrayAdapter extends ArrayAdapter<RosterEntry> {

        public ChatArrayAdapter(Context context, RosterEntry[] values) {
            super(context, R.layout.user_list_item, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Tag tag;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.user_list_item, parent, false);
                convertView.setTag(tag = new Tag(convertView));
            } else {
                tag = (Tag) convertView.getTag();
            }

            final RosterEntry entry = getItem(position);

            tag.username.setText(entry.getUser());

            tag.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Bundle args = new Bundle();
                    args.putString("userid", entry.getUser());

                    ChatFragment chat = new ChatFragment();
                    chat.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, chat).commitAllowingStateLoss();

                }
            });

            return  convertView;
        }

        final class Tag {

            final TextView username;
            final View item;

            Tag(View view){

                username = (TextView) view.findViewById(R.id.username);
                item = view.findViewById(R.id.item);
            }
        }

    }

    public ChatListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("MESSAGE"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            android.util.Log.d("receiver", "Got message: " + message);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        getActivity().setTitle("Чат");

        mList = (ListView)view.findViewById(R.id.list);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        Roster roster = Roster.getInstanceFor(getApp().getConnection());
        Collection<RosterEntry> entries = roster.getEntries();

        ChatArrayAdapter adapter = new ChatArrayAdapter(getActivity(), entries.toArray(new RosterEntry[0]));
        mList.setAdapter(adapter);

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mList = null;
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mMessageReceiver);
        mMessageReceiver = null;
    }

    public MyJabbApp getApp(){
        return ((ChatActivity)getActivity()).getApp();
    }

}
