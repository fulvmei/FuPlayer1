package com.chengfu.fuplayer.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.chengfu.fuplayer.FuLog;
import com.chengfu.fuplayer.demo.adapter.MediaGroupListAdapter;
import com.chengfu.fuplayer.demo.bean.Media;
import com.chengfu.fuplayer.demo.bean.MediaGroup;
import com.chengfu.fuplayer.player.sys.SysPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {

    private ExpandableListView expandableListView;

    private MediaGroupListAdapter mediaGroupListAdapter;
    private List<MediaGroup> mediaGroupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expandableListView = findViewById(R.id.expandableListView);

        expandableListView.setOnChildClickListener(this);

        mediaGroupList = getMediaGroupList();
        mediaGroupListAdapter = new MediaGroupListAdapter(mediaGroupList);

        expandableListView.setAdapter(mediaGroupListAdapter);

        mediaGroupListAdapter.notifyDataSetChanged();
    }

    private List<MediaGroup> getMediaGroupList() {
        List<MediaGroup> mediaGroups = new ArrayList<>();
        JSONArray ja = null;
        try {
            ja = new JSONArray(getMediaGroupListString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {
                try {
                    mediaGroups.add(parsedMediaGroup(ja.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return mediaGroups;
    }

    private MediaGroup parsedMediaGroup(JSONObject jo) {
        MediaGroup mediaGroup = null;
        if (jo != null) {
            mediaGroup = new MediaGroup();
            mediaGroup.setName(jo.optString("name"));
            mediaGroup.setMediaList(parsedMediaList(jo.optJSONArray("media_list")));
        }
        return mediaGroup;
    }

    private List<Media> parsedMediaList(JSONArray ja) {
        List<Media> mediaList = null;
        if (ja != null) {
            mediaList = new ArrayList<>();
            for (int i = 0; i < ja.length(); i++) {
                try {
                    mediaList.add(parsedMedia(ja.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return mediaList;
    }

    private Media parsedMedia(JSONObject jo) {
        Media media = null;
        if (jo != null) {
            media = new Media();
            media.setName(jo.optString("name"));
            media.setPath(jo.optString("path"));
            media.setTag(jo.optString("tag"));
        }
        return media;
    }

    private String getMediaGroupListString() {
        InputStream inputStream = null;
        String media_list = null;
        try {
            inputStream = getAssets().open("media_list.json");
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            media_list = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return media_list;
    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                int childPosition, long id) {
        Media media = mediaGroupList.get(groupPosition).getMediaList().get(childPosition);
        Intent intent=new Intent(this,PlayerActivity.class);
        intent.putExtra("media",media);
        startActivity(intent);
        return true;
    }
}
