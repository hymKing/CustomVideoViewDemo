package com.hym.customvideoviewdemo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.flyup.common.utils.UIUtils;
import com.flyup.net.image.ImageLoader;
import com.hym.hymvideoview.CustomVideoView;
import com.hym.hymvideoview.HymMediaController;
import com.hym.hymvideoview.HymVideoView;


public class SigleTestActivity extends Activity implements OnClickListener,CustomVideoView.ExtendVideoViewCallBack {
	private CustomVideoView cvv_video;
	private Button btn_set_path;
	private Button btn_set_err_path;
	private HymMediaController mMediaController;
	private ImageView preImg;
	private View mVideoLayout;
	private View mBottomLayout;

	private CustomVideoView customVideoView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_acustom_video_act);
		mVideoLayout=findViewById(R.id.video_layout);
		mBottomLayout=findViewById(R.id.ll_bottom);
		cvv_video=(CustomVideoView) findViewById(R.id.cvv_video);
		btn_set_path=(Button)findViewById(R.id.btn_set_path);
		btn_set_path.setOnClickListener(this);
		btn_set_err_path=(Button)findViewById(R.id.btn_set_err_path);
		btn_set_err_path.setOnClickListener(this);
		cvv_video.setOnExtendVideoViewCallBack(this);
		customVideoView=(CustomVideoView)findViewById(R.id.cvv_video_2);

	}
	//    http://www.boomq.com/apollo/video/2016/7/5/test19_7M.mp4
//    http://www.boomq.com/apollo/video/2016/7/5/test4_79M.mp4
//    http://www.boomq.com/apollo/video/2016/7/5/test8_1M.mp4
	String testUrl="http://www.boomq.com/apollo/video/2016/7/5/test4_79M.mp4";
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
				cvv_video.setVideoFirstFrame(firstFramePath);
				cvv_video.setTag(testUrl);
				cvv_video.setVideoPath(cvv_video.getTag().toString());

				break;
			case R.id.btn_set_err_path:
				cvv_video.start();
//				customVideoView.setVideoPathAndFirstFrame(testUrl,firstFramePath);
//				customVideoView.start();

				break;
		default:
			break;
		}
	}
	@Override
	public void onScaleChange(boolean isFullscreen) {
//		if(isFullscreen){
//			//全屏显示
//			ViewGroup.LayoutParams layoutParams=mVideoLayout.getLayoutParams();
//			layoutParams.width=ViewGroup.LayoutParams.MATCH_PARENT;
//			layoutParams.height=ViewGroup.LayoutParams.MATCH_PARENT;
//			mVideoLayout.setLayoutParams(layoutParams);
//			mBottomLayout.setVisibility(View.GONE);
//		}else{
//			//非全屏显示
//			ViewGroup.LayoutParams layoutParams=mVideoLayout.getLayoutParams();
//			layoutParams.width=ViewGroup.LayoutParams.MATCH_PARENT;
//			layoutParams.height= UIUtils.dip2px(200);
//			mVideoLayout.setLayoutParams(layoutParams);
//			mBottomLayout.setVisibility(View.VISIBLE);
//		}
	}

	@Override
	public void onPause(MediaPlayer mediaPlayer) {

	}

	@Override
	public void onStart(MediaPlayer mediaPlayer) {
	}

	@Override
	public void onBufferingStart(MediaPlayer mediaPlayer) {

	}

	@Override
	public void onBufferingEnd(MediaPlayer mediaPlayer) {

	}

	@Override
	protected void onPause() {
		super.onPause();
		//cvv_video.onActivityOnPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//cvv_video.onActivityOnRestart();
	}


}
