package com.hym.customvideoviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.hym.customvideoviewdemo.view.CustomVideoView;


public class SigleTestActivity extends Activity implements OnClickListener {
	private CustomVideoView cvv_video;
	private Button btn_set_path;
	private Button btn_set_err_path;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_acustom_video_act);
		cvv_video=(CustomVideoView) findViewById(R.id.cvv_video);
		btn_set_path=(Button)findViewById(R.id.btn_set_path);
		btn_set_path.setOnClickListener(this);
		btn_set_err_path=(Button)findViewById(R.id.btn_set_err_path);
		btn_set_err_path.setOnClickListener(this);

	}
	String firstFramePath="http://www.boomq.com/resize/photo/720/770/2016/5/27/ea7881633c8d4d798ac63f7a1031110a.png";
	String errFramePath="http://www.boomq.com/resize/photo/720/770/2016/5/27/ea7881633c8d4d798ac63f7a1031110a.pn";
	String vedioPath="http://www.boomq.com/apollo/video/2016/5/27/310d8194db424342a0a2fed472929d4a.mp4";
	String vedioPath2="http://qiubeiai.com//resources/vedio/template/01.mp4";
	String errorPath="http://www.boomq.com/apollo/video/2016/5/27/310d8194db424342a0a2fed472929d4a.mp";
	int count=0;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.btn_set_path:
				if(count%2==0){
					cvv_video.setVideoPathAndFirstFrame(vedioPath,firstFramePath);
				}else{
					cvv_video.setVideoPathAndFirstFrame(vedioPath2,firstFramePath);
				}
				count++;
				break;
			case R.id.btn_set_err_path:
				//cvv_video.setVideoPathAndFirstFrame(errorPath,errFramePath);
				cvv_video.stopVideo();
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
}
