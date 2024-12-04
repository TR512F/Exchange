package com.bubnov.v5.model;


public class Message {
    public Message(String text, String to) {
        this.text = text;
        this.to = to;
    }

    private String text;

    private String to;

    public String getText() {
        return text;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}