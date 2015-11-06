package com.myjabb;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ChatListFragment chatList = new ChatListFragment();
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, chatList).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AddContackFragment addContact = new AddContackFragment();
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, addContact).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && getSupportFragmentManager().getBackStackEntryCount() > 1) {

            getSupportFragmentManager().popBackStack();
        } else {
            Intent loginActivity = new Intent(ChatActivity.this, LoginActivity.class);
            ChatActivity.this.startActivity(loginActivity);
            finish();
        }

        return false;
    }

    public MyJabbApp getApp(){
        return (MyJabbApp)getApplication();
    }

}
