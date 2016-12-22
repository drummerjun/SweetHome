package com.evsp.sweethome.adapters;

public class MessageItem {

    private String mString = "";
    private boolean isIncoming = false;
    private String mTime = "";
    private String mOrigin = "";

    public MessageItem(String string, boolean incoming, String time) {
        mString = string;
        isIncoming = incoming;
        mTime = time;
    }

    public String getMessage() {
        return mString;
    }

    public void setMessage(String string) {
        mString = string;
    }

    public boolean getIncoming() {
        return isIncoming;
    }

    public void setIncoming(boolean incoming) {
        isIncoming = incoming;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getOrigin() {
        return mOrigin;
    }

    public void setOrigin(String value) {
        mOrigin = value;
    }
}