package com.mka.memorylocker.Util;

import android.app.Application;
import android.content.res.Resources;

public class MemoryApi extends Application {

    private String user;
    private String userId;
    private String docId;
    private static Resources resources;

    public MemoryApi() {
    }

    private static MemoryApi instance;
    public static MemoryApi getInstance() {
        if (instance == null) instance = new MemoryApi();
        return instance;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        resources = getResources();

    }

    public static Resources get() {
        return resources;
    }
}
