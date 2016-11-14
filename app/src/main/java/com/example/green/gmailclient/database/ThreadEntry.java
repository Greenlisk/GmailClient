package com.example.green.gmailclient.database;

import com.google.api.services.gmail.model.Thread;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by green on 11/13/16.
 */

public class ThreadEntry extends RealmObject {
    @PrimaryKey
    private String id;
    private String subject;
    private RealmList<MessageEntry> messages;
    private Boolean read;
    private String from;
    private Long lastDate;

    public ThreadEntry(){}

    public ThreadEntry(Thread thread){
        this.id = thread.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public RealmList<MessageEntry> getMessages() {
        return messages;
    }

    public void setMessages(RealmList<MessageEntry> messages) {
        this.messages = messages;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getLastDate() {
        return lastDate;
    }

    public void setLastDate(Long lastDate) {
        this.lastDate = lastDate;
    }
}
