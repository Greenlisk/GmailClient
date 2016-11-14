package com.example.green.gmailclient;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by green on 11/13/16.
 */

public class GmailClient extends Application {
    public static final String prefsName = "prefsName";

    public GmailClient(){}

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
