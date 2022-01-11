package com.android.linkedphotoShSonya.comments;

public class Comment {
    private String imageIdSender;
    private String uidSender;
    private String textComment;
    private String time;
    private String userName;

    public String getImageIdSender() {
        return imageIdSender;
    }

    public void setImageIdSender(String imageIdSender) {
        this.imageIdSender = imageIdSender;
    }

    public String getUidSender() {
        return uidSender;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUidSender(String uidSender) {
        this.uidSender = uidSender;
    }

    public String getTextComment() {
        return textComment;
    }

    public void setTextComment(String textComment) {
        this.textComment = textComment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
