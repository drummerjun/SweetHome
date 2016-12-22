package com.evsp.sweethome.adapters;

public class AlertItem {
    private int id = 0;
    private String type = "";
    private String time = "";
    private String string = "";

    public AlertItem(int id, String type, String string, String time) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.string = string;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return string;
    }

    public void setMessage(String string) {
        this.string = string;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}