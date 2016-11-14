package com.example.green.gmailclient.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.green.gmailclient.R;
import com.example.green.gmailclient.database.ThreadEntry;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by green on 11/13/16.
 */

public class ThreadAdapter extends RealmBaseAdapter<ThreadEntry> {
    private final static String LOG_TAG = "ThreadAdapter";
    private Context context;

    public ThreadAdapter(Context context, RealmResults<ThreadEntry> data){
        super(context, data);
        this.context = context;
    }

    private class ViewHolder{
        public final TextView snippet;
        public final TextView authors;

        public ViewHolder(View v){
            snippet = (TextView)v.findViewById(R.id.thread_title_view);
            authors = (TextView)v.findViewById(R.id.thread_authors_view);
        }
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
        ViewHolder viewHolder;
        ThreadEntry thread = adapterData.get(i);
        if(view == null){
            output = LayoutInflater.from(context).inflate(R.layout.item_threads_list, viewGroup, false);
            viewHolder = new ViewHolder(output);
            output.setTag(viewHolder);
        } else {
            output = view;
            viewHolder = (ViewHolder) output.getTag();
        }
        viewHolder.snippet.setText(thread.getSubject());
        return output;
    }

    @Nullable
    @Override
    public ThreadEntry getItem(int position) {
        return super.getItem(position);
    }
}
