package com.example.green.gmailclient.database;

import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.Thread;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;

/**
 * Created by green on 11/13/16.
 */

public class BaseFetcher {
    private final static String LOG_TAG = "BaseFetcher";
    private com.google.api.services.gmail.Gmail mService;
    private Realm realm;
    private final static String user = "me";

    public BaseFetcher(GoogleAccountCredential credential){
        realm = Realm.getDefaultInstance();
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Gmail API Android Quickstart")
                .build();
    }

    public Boolean fetchDatabase(){

        return true;
    }
}
