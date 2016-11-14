package com.example.green.gmailclient.database;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import io.realm.RealmObject;

/**
 * Created by green on 11/13/16.
 */

public class HeaderEntry extends RealmObject {
    private String name;
    private String value;

    public HeaderEntry(){}

    public HeaderEntry(MessagePart messagePart){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
