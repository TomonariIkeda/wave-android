package jp.cleartouch.postcast;

import java.io.IOException;
import java.util.ArrayList;


import android.os.Bundle;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class MainActivity extends Activity {

	private static final String TAG = "CasterPlayer";
	
	private CasterPlayer player;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.getWindow()
			.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN | LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		
		String audioUrl = "http://dev.cleartouch.jp/akb_radio.mp3";
		String dataUrl = "http://dev.cleartouch.jp/postdata.json";
		
		// get view instances
		RelativeLayout screen = (RelativeLayout) findViewById(R.id.screenRelativeLayout);
		ProgressBar loadingProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		TextView elapsedTextView = (TextView) findViewById(R.id.elapsedTextView);
		TextView durationTextView = (TextView) findViewById(R.id.durationTextView);
		RelativeLayout control = (RelativeLayout) findViewById(R.id.controlRelativeLayout);
		SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setThumbOffset((int) Helpers.convertDpToPixel(8, this));
		
		try{
			player = new CasterPlayer(this, screen, control, seekBar, elapsedTextView, durationTextView, loadingProgressBar);
			player.setDataSource(audioUrl, dataUrl);		
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + audioUrl, ex);
			return;
		}
		catch(IllegalArgumentException ex){
			Log.w(TAG, "Unable to open content: " + audioUrl, ex);
			return;
		}
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;
		
		//Log.d (TAG, "Dim of Display: " + height + "," + width);
		EditText et = (EditText) findViewById(R.id.play_edit_text);
		et.setOnTouchListener(new View.OnTouchListener(){
		    public boolean onTouch(View view, MotionEvent event) {                                                       
		         Log.d (TAG, "EditText onTouch()");
		         if(event.getAction() == MotionEvent.ACTION_DOWN){
		        	 if(player.isPlaying()){
		     			player.pause();
		     		}
		         }
		         //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);                
		         return false;
		    }
		});
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//Log.d (TAG, "onTouchEvent()");
		/*
		if(event.getAction() == MotionEvent.ACTION_DOWN 
				&& ! player.isHideControlAnimationRunning()){
			if(player.isControlVisible()) {
				player.togglePlayPause();
			}else{
				player.showControl();
				player.startCountForHideControl();
			}
		}
		*/
		return super.onTouchEvent(event);
	}
	
	// called from CasterPlayer
	/*
	public void onPrepared() {
		EditText et = (EditText) findViewById(R.id.play_edit_text);
		et.setEnabled(true);
	}
	*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// 他のアプリに移ったとき。例）電話着信など。
		if(player.isPrepared() && player.isPlaying())
			player.pause();
	}	
	
	@Override
	public void onResume() {
		super.onResume();
		// アプリを立ち上げたとき。
		// 他のアプリから戻ったとき。
		// 他のアクティビティからもどったとき。
		
		// -> Pause状態になっているため、特になにもしない。
		//if(player.isPrepared() && ! player.isPlaying()){
		//	player.start();
		//}
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    
	    player.release(); 
		player = null;
		
	    Log.d(TAG, "onDestroy");
	}

}
