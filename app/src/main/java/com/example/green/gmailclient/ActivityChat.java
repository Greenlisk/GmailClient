package com.example.green.gmailclient;

import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.green.gmailclient.adapters.MessageAdapter;
import com.example.green.gmailclient.database.MessageEntry;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ActivityChat extends AppCompatActivity implements ListView.OnItemClickListener{
    private final static String LOG_TAG = "ActivityChat";
    private MessageAdapter messageAdapter;
    RealmResults<MessageEntry> messageEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Chat");

        String id = getIntent().getStringExtra("id");
        Realm realm = Realm.getDefaultInstance();
        messageEntries = realm.where(MessageEntry.class).equalTo("threadId", id).findAllSorted("internalDate", Sort.DESCENDING);

        ListView chatView = (ListView)findViewById(R.id.messages_list);
        messageAdapter = new MessageAdapter(this, messageEntries);
        chatView.setAdapter(messageAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        MessageAdapter.ViewHolder viewHolder = (MessageAdapter.ViewHolder)view.getTag();
        viewHolder.message.setMaxLines(Integer.MAX_VALUE);
        messageAdapter.updateData(messageEntries);

    }
}
