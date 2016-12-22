package com.evsp.sweethome.adapters;

public class InputPair {
    private String server;
    private String port;

    public InputPair(String server, String port) {
        this.server = server;
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public String getPort() {
        return port;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
