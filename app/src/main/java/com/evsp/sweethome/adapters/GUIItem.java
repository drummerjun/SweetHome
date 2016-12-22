package com.evsp.sweethome.adapters;

import com.evsp.sweethome.Constants;

public class GUIItem {
    //common
    private int image_id;
    private int id;
    private String name;
    private String type;
    private AlertItem lastAlert;

    //plug/power
    private boolean loaded;  //removed|connected
    private boolean overload;
    private boolean power;  //on|off
    private int watt; //active_power

    //wall switch
    private String value;  //switch_action='DownLong|DownShort|UpLong|UpShort'
    private int battery;

    //motion
    //smoke
    //keypad
    //siren
    private boolean tamper;

    //flood detector - flood='on|off'
    //magnetic - open|close
    //smoke - fire='alarm'
    //motion - motion='triggered'
    private boolean trigger;

    //siren - low battery
    private boolean low_batt;

    //keypad & remote
    private String security;

    //temp & humid
    private double temp;
    private int humidity;

    private boolean unread;

    public GUIItem() {
        image_id = Integer.MIN_VALUE;
        id = Integer.MIN_VALUE;
        name = "";
        type = "";
        loaded = false;
        overload = false;
        power = false;
        battery = Integer.MIN_VALUE;
        watt = Integer.MIN_VALUE;
        temp = Double.MIN_VALUE;
        humidity = Integer.MIN_VALUE;
        trigger = false;
        tamper = false;
        value = "";
        security = "";
        unread = false;
    }

    public GUIItem(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;

        loaded = false;
        overload = false;
        power = false;
        battery = Integer.MIN_VALUE;
        watt = Integer.MIN_VALUE;
        temp = Double.MIN_VALUE;
        humidity = Integer.MIN_VALUE;
        trigger = false;
        tamper = false;
        value = "";
        security = "";
        unread = false;
    }

    public GUIItem(int id, String type) {
        this.id = id;
        this.type = type;

        name = "";
        loaded = false;
        overload = false;
        power = false;
        battery = Integer.MIN_VALUE;
        watt = Integer.MIN_VALUE;
        temp = Double.MIN_VALUE;
        humidity = Integer.MIN_VALUE;
        trigger = false;
        tamper = false;
        value = "";
        security = "";
        unread = false;
    }

    public void setImageId(int id) {
        image_id = id;
    }

    public int getImageId() {
        return image_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String label) {
        this.name = label;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean getLoaded() {
        return loaded;
    }

    public void setOverload(boolean overload) {
        this.overload = overload;
    }

    public boolean getOverload() {
        return overload;
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public boolean getPower() {
        return power;
    }

    public void setLowBatt(boolean low) {
        low_batt = low;
    }

    public boolean getLowBatt() {
        return low_batt;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getBattery() {
        return battery;
    }

    public void setWatt(int watt) {
        battery = 0;
        this.watt = watt;
    }

    public int getWatt() {
        return watt;
    }

    public void setThermo(double temp, int humidity) {
        this.temp = temp;
        this.humidity = humidity;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getTemp() {
        return temp;
    }

    public void setHumidity(int value) {
        humidity = value;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setTrigger(boolean value) {
        trigger = value;
    }

    public boolean getTrigger() {
        return trigger;
    }

    public void setTamper(boolean value) {
        tamper = value;
    }

    public boolean getTamper() {
        return tamper;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setSecurity(String value) {
        security = value;
    }

    public String getSecurity() {
        return security;
    }

    public void setAlert(AlertItem alert) {
        lastAlert = alert;
    }

    public AlertItem getAlert() {
        return lastAlert;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public boolean getUnread() {
        return unread;
    }

    public void update(GUIItem item) {
        name = item.getName();
        switch(item.getType()) {
            case Constants.DEVICE_THERMO:
                battery = item.getBattery();
                temp = item.getTemp();
                humidity = item.getHumidity();
                break;
            case Constants.DEVICE_PLUG:
                loaded = item.getLoaded();
                overload = item.getOverload();
                power = item.getPower();
                watt = item.getWatt();
                break;
            case Constants.DEVICE_WALL_SWITCH:
                battery = item.getBattery();
                //value = item.getValue();
                power = item.getPower();
                //unread = item.getUnread();
                break;
            case Constants.DEVICE_MOTION:
                power = item.getPower();
                //unread = item.getUnread();
                break;
            case Constants.DEVICE_FLOOD:
                trigger = item.getTrigger();
                break;
            case Constants.DEVICE_MAGNETIC:
                trigger = item.getTrigger();
                battery = item.getBattery();
                break;
            case Constants.DEVICE_SMOKE:
                trigger = item.getTrigger();
                tamper = item.getTamper();
                battery = item.getBattery();
                //unread = item.getUnread();
                break;
            case Constants.DEVICE_KEYPAD:
                tamper = item.getTamper();
                battery = item.getBattery();
                //unread = item.getUnread();
                break;
            case Constants.DEVICE_SIREN:
                power = item.getPower();
                //unread = item.getUnread();
                break;
            default:
                break;
        }
    }
}
