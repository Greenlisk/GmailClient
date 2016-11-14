package com.example.green.gmailclient.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by green on 11/13/16.
 */

public class MessageEntry extends RealmObject {
    @PrimaryKey
    private String id;
    private String threadId;
    private String body;
    private Long internalDate;
    private String from;

    public MessageEntry(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getInternalDate() {
        return internalDate;
    }

    public void setInternalDate(Long internalDate) {
        this.internalDate = internalDate;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
