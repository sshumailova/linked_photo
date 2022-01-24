package com.sonya_shum.linkedphotoShSonya.chat;

public class AwesomeMessage {
   private String text;
    private String name;
    private String sender;
    private String recipient;
    private String imageUrl;
    private boolean isMine;
    private String time;
    public AwesomeMessage(){

    }

    public AwesomeMessage(String text, String name, String imageUrl, String sender, String recipient, boolean isMine) {
        this.text = text;
        this.name=name;
        this.sender=sender;
        this.recipient= recipient;
        this.imageUrl=imageUrl;
        this.isMine=isMine;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
