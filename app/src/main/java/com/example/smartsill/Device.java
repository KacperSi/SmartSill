package com.example.smartsill;

import androidx.annotation.NonNull;

public class Device {
    private String name;
    private String ip;
    private String uuid;
    private String password;

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getPassword() {
        return password;
    }
    public String getUUID() {
        return uuid;
    }

    public Device(String name, String ip, String uuid, String password) {
        this.name = name;
        this.ip = ip;
        this.uuid = uuid;
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
