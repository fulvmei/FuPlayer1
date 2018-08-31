package com.chengfu.fuplayer.demo.bean;

import java.io.Serializable;

public class Media implements Serializable {
    private String name;
    private String path;
    private String tag;

    public Media() {

    }

    public Media(String name, String path, String tag) {
        this.name = name;
        this.path = path;
        this.tag = tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
