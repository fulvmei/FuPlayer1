package com.chengfu.fuplayer.demo.bean;

import java.util.List;

public class MediaGroup {

    public String name;
    public List<Media> mediaList;

    public MediaGroup() {

    }

    public MediaGroup(String name, List<Media> mediaList) {
        this.name = name;
        this.mediaList = mediaList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }
}
