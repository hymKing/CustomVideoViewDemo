package com.hym.customvideoviewdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
//    http://www.boomq.com/apollo/video/2016/7/5/test19_7M.mp4
//    http://www.boomq.com/apollo/video/2016/7/5/test4_79M.mp4
//    http://www.boomq.com/apollo/video/2016/7/5/test8_1M.mp4
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_sigle).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent=null;
        switch (v.getId()){
            case R.id.btn_sigle:
                intent=new Intent(this,SigleTestActivity.class);
                startActivity(intent);
                break;
        }
    }
}
