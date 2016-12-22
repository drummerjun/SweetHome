package com.evsp.sweethome.adapters;

public class DrawerItem {

	String ItemName;
	int imgResID;
	String title;
    int deviceID;
    int notifications;
    boolean gatewayStatus;

	public DrawerItem(String itemName, int imgResID, int deviceID) {
		ItemName = itemName;
		this.imgResID = imgResID;
        this.deviceID = deviceID;
	}

	public DrawerItem(String title) {
		this(null, 0, 0);
		this.title = title;
	}

	public String getItemName() {
		return ItemName;
	}

	public void setItemName(String itemName) {
		ItemName = itemName;
	}

	public int getImgResID() {
		return imgResID;
	}

	public void setImgResID(int imgResID) {
		this.imgResID = imgResID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

    public void setDeviceID(int id) {
        this.deviceID = id;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public void setNotifications(int notifications) {
        this.notifications = notifications;
    }

    public int getNotifications() {
        return notifications;
    }

    public void setGatewayStatus(boolean connected) {
        gatewayStatus = connected;
    }

    public boolean getGatewayStatus() {
        return gatewayStatus;
    }
}
