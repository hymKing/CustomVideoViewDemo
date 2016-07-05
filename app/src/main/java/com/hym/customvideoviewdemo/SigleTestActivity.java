package com.hym.customvideoviewdemo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.flyup.net.image.ImageLoader;
import com.hym.customvideoviewdemo.view.CustomVideoView;
import com.hym.hymvideoview.HymMediaController;
import com.hym.hymvideoview.HymVideoView;


public class SigleTestActivity extends Activity implements OnClickListener {
	private HymVideoView cvv_video;
	private Button btn_set_path;
	private Button btn_set_err_path;
	private HymMediaController mMediaController;
	private ImageView preImg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_acustom_video_act);
		cvv_video=(HymVideoView) findViewById(R.id.cvv_video);
		cvv_video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				preImg.setVisibility(View.GONE);
				return false;
			}
		});
		cvv_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				preImg.setVisibility(View.GONE);
			}
		});
		preImg=(ImageView) findViewById(R.id.pre_on_uv_img);
		mMediaController = (HymMediaController) findViewById(R.id.media_controller);
		cvv_video.setMediaController(mMediaController);
		btn_set_path=(Button)findViewById(R.id.btn_set_path);
		btn_set_path.setOnClickListener(this);
		btn_set_err_path=(Button)findViewById(R.id.btn_set_err_path);
		btn_set_err_path.setOnClickListener(this);

	}
	//    http://www.boomq.com/apollo/video/2016/7/5/test19_7M.mp4
//    http://www.boomq.com/apollo/video/2016/7/5/test4_79M.mp4
//    http://www.boomq.com/apollo/video/2016/7/5/test8_1M.mp4
	String testUrl="http://www.boomq.com/apollo/video/2016/7/5/test19_7M.mp4";
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
				ImageLoader.load(preImg, firstFramePath,0, R.mipmap.ic_launcher,true);

				break;
			case R.id.btn_set_err_path:
				btn_set_err_path.setText("播放");
				cvv_video.setVideoPath(testUrl);
				cvv_video.start();
				break;
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
