package com.hym.customvideoviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.hym.hymvideoview.CustomVideoView;

public class FullScreenVideoAct extends Activity {


    CustomVideoView video;
    String firstFrameUrl = "";
    String videoUrl = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_full_screen_video);
        video = (CustomVideoView) findViewById(R.id.video);
        video.setTouchFinishSwitcher(true);
        init();
    }

    public void init() {
        firstFrameUrl = getIntent().getStringExtra("firstFrameUrl");
        videoUrl = getIntent().getStringExtra("videoUrl");
        if (!TextUtils.isEmpty(firstFrameUrl)) {
            video.setVideoFirstFrame(firstFrameUrl);
        }
        if (!TextUtils.isEmpty(videoUrl)) {
            video.setVideoPath(videoUrl);
            video.start(false);
        }
    }

    @Override
    protected void onPause() {
        video.onActivityOnPause();
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        video.onActivityOnRestart();
    }
}
