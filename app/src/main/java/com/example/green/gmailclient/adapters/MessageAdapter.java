package com.example.green.gmailclient.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.green.gmailclient.GmailClient;
import com.example.green.gmailclient.R;
import com.example.green.gmailclient.database.MessageEntry;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by green on 11/13/16.
 */

public class MessageAdapter extends RealmBaseAdapter<MessageEntry> {
    private final static String LOG_TAG = "MessageAdapter";

    public MessageAdapter(Context context, RealmResults data){
        super(context, data);
    }

    public class ViewHolder{
        public final TextView from;
        public final TextView message;

        public ViewHolder(View v, int type){
            if(type == 1) {
                from = (TextView) v.findViewById(R.id.message_from_outcoming);
                message = (TextView) v.findViewById(R.id.message_text_outcoming);
            } else {
                from = (TextView) v.findViewById(R.id.message_from_incoming);
                message = (TextView) v.findViewById(R.id.message_text_incoming);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Boolean type = adapterData.get(position).getFrom().contains(context.getSharedPreferences(GmailClient.prefsName, Context.MODE_PRIVATE).getString("email", null));
        return type ? 1 : 2;
    }

    @Override
    public int getCount() {
        if(adapterData != null) {
            if (adapterData.size() > 10) {
                return 10;
            }
        }
        return super.getCount();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View output;
        MessageAdapter.ViewHolder viewHolder;
        MessageEntry message = adapterData.get(i);

            if(getItemViewType(i) == 2) {
                output = LayoutInflater.from(context).inflate(R.layout.item_messages_list_incoming, viewGroup, false);
                viewHolder = new MessageAdapter.ViewHolder(output, getItemViewType(i));
            } else {
                output = LayoutInflater.from(context).inflate(R.layout.item_messages_list_outcoming, viewGroup, false);
                viewHolder = new MessageAdapter.ViewHolder(output, getItemViewType(i));
            }
            output.setTag(viewHolder);

        viewHolder.from.setText(message.getFrom());
        viewHolder.message.setText(message.getBody());
        return output;
    }
}
