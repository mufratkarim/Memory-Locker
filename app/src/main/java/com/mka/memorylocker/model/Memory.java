package com.mka.memorylocker.model;

import android.text.format.DateUtils;

import com.google.firebase.Timestamp;

public class Memory {

    private String title;
    private String memory;
    private String imageUrl;
    private String userId;
    private Timestamp timeAdded;
    private String userName;

    public Memory() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return userName + "'s Memories\nHeading: "
                + title + ".\nStatus: " + memory +
                ".\nTime: " + DateUtils.getRelativeTimeSpanString(timeAdded.getSeconds() * 1000) + ".\n";
    }
}
