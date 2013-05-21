package jp.cleartouch.wave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import jp.cleartouch.libs.rest.RestProcessor;
import jp.cleartouch.libs.rest.RestProcessor.RestProcessorListener;
import jp.cleartouch.postcast.R;
import jp.cleartouch.wave.PostDataBuffer.PostDataBufferListener;
import jp.cleartouch.wave.PostDataReader.PostDataReaderListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import jp.cleartouch.sqlite.WaveSQLiteHelper;

public class WavePlayer extends MediaPlayer{
	
	public WavePlayer(Activity activity, RelativeLayout screen, RelativeLayout control, SeekBar seekBar, 
							TextView elapsed, TextView duration, ProgressBar loading, String mediaId,
							EditText postEditText, Button postButton, String userName, byte[] userThumbData){
		super();
		setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		// set activity and screen where PostView will be displayed
		this.activity = activity;
		this.screen = screen;
		this.loadingProgressBar = loading;
		this.seekBar = seekBar;
		this.durationTextView = duration;
		this.elapsedTextView = elapsed;
		this.control = control;
		this.mediaId = mediaId;
		this.postEditText = postEditText;
		this.postButton = postButton;
		this.userName = userName;
		this.userThumbData = userThumbData;
		
		// setup PostData buffer and displayQueue
		pdBuffer = new PostDataBuffer(this);
		freePVs = new ArrayList<PostView>();
		onScreenPVs = new HashSet<PostView>();
		displayedAtSeekPVs = new HashSet<PostView>();
		postDataReader = new PostDataReader(this);
		restProcessor = new RestProcessor(this);
		
		// register listeners
		this.setOnPreparedListener(mPreparedListener);
		this.setOnInfoListener(mInfoListener);
		this.setOnErrorListener(mErrorListener);
		this.setOnBufferingUpdateListener(mBufferingListener);
		this.setOnSeekCompleteListener(mSeekCompleteListener);
		this.seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		this.screen.setOnTouchListener(mScreenTouchListener);
		this.postDataReader.setOnPostDataReaderListener(mPostDataReaderListener);
		this.pdBuffer.setOnPostDataBufferListener(mPostDataBufferListener);
		this.postButton.setOnClickListener(mPostButtonOnClickListener);
		this.restProcessor.setOnRestProcessorListener(mRestProcessorListener);
		
		// instantiate postviews and put it in queue
		PostView tmp;
		for ( int i = 0; i < NUM_OF_PV_INSTANCE+1; ++i ) {
			tmp = new PostView(activity);
			screen.addView(tmp);
			
			// set animation listener
			SlideInAnimation slideInAnimation = tmp.getSlideInAnimation();
			NewPostAnimation newPostAnimation = tmp.getNewPostAnimation(); 
	        slideInAnimation.setAnimationListener(slideInAnimationListener);
	        newPostAnimation.setAnimationListener(newPostAnimationListener);
			addToFreeQueue(tmp);
		}// end of for loop
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.media.MediaPlayer#setDataSource(java.lang.String)
	 */
	public void setDataSource(String audio_path, String data_path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException{
		
		// get first 1 minutes of PostData
		startPositionInSec = 0;
		postDataReader.startRetrievingPostData(1);
		
		super.setDataSource(audio_path);
		super.prepareAsync();
	}
	
	
	/*
	 * Called from OnBufferingUpdateListener() repeatedly
	 * when CasterPlayer is in playing state. displayPV() will be called right after.
	 * 
	 */
	public void displayPostViews(int sec){
		//Log.e (TAG, "setDisplayQueue("+sec+")");

		final ArrayList<PostData> listToBeDisplayed = pdBuffer.getPdListAt(sec);	
		// listToBeDisplayed set to null when there is no PostData at given sec.
		if(listToBeDisplayed != null){
			
			activity.runOnUiThread(new Runnable() {
	    		public void run() {
	    			
	    			// set PostData to PostView
	    			for(PostData postData: listToBeDisplayed){
		    			if(freePVs.size() - 1 > 0){
		    				// retrieve available PostView instance for display.
		    				// reserve one PostView for new post.
		    				PostView nextPV = freePVs.remove(0);				
		    				String color = (userName.compareTo(postData.getUserName())==0) ? "yellow" : "white";
		    				nextPV.setData(postData.getText(), postData.getThumbData(), postData.getUserName(), 
		    						postData.getCreatedDate(), postData.getCreatedTime(), postData.getY(), postData.getDisplayAt(),color, postData.getSlideInAnimationVerocity());
					    	nextPV.startSlideIn();
					    	onScreenPVs.add(nextPV);
					    	if( ! isAnimationRunning )
					    		nextPV.pauseAnimation();
		    			}else{
		    				// for now, ignore and don't display.
		    				Log.e (TAG, "No free PostView instance!!");
		    			}
	    			} // end of while loop
	    			
	    		}// end of run()
			});// end of runOnUiThread()
			
		}// end of if stmt
			
	}
	
	public void pauseAnimation(){
		isAnimationRunning = false;
        for(PostView pv: onScreenPVs){
        	pv.pauseAnimation();
        }        
	}
	
	public void resumeAnimation(){
		isAnimationRunning = true;
        for(PostView pv: onScreenPVs){
        	pv.resumeAnimation();
        }
	}	
	
	public void addToFreeQueue(PostView pv){
		Log.d (TAG, "addToFreeQueue()");
		freePVs.add(pv);
	}
	
	private void startPlayer(){
		if( isPrepared() ){
			if( ! isPlaying() ){
				Log.d (TAG, "player started.");
				hideLoading();
				super.start();
				resumeAnimation();
				elapsedTextView.clearAnimation();
				startUpdatingSeekBar();
				startUpdatingElapsedText();
			}
		}
	}
	
	public void pausePlayer(){
		if( isPrepared() ){
			if( isPlaying() ) {
				Log.d (TAG, "player paused.");
				super.pause();
				pauseAnimation();
				stopUpdatingSeekBar();
				stopUpdatingElapsedText();
				// start blinking
				Animation anim = new AlphaAnimation(0.0f, 1.0f);
				anim.setDuration(50); //You can manage the time of the blink with this parameter
				anim.setStartOffset(500);
				anim.setRepeatMode(Animation.REVERSE);
				anim.setRepeatCount(Animation.INFINITE);
				elapsedTextView.startAnimation(anim);
			}
		}
	}
	
	public void togglePlayPause(){
		if( isPrepared() ){
			if(isPlaying()){
				pausePlayer();
			}else{
				startPlayer();
			}
		}
	}
	
	public void showLoading(){
		loadingProgressBar.setVisibility(ProgressBar.VISIBLE);
		loadingProgressBar.bringToFront();
	}
	
	public void hideLoading(){
		loadingProgressBar.setVisibility(ProgressBar.INVISIBLE);
	}
	
	public void release(){
		super.release();
	}
	
	public int calculateYForNewPost(int targetSec, final int width, final int height){
		Log.e(TAG, "calculateYForNewPost(" + targetSec + "," + width + "," + height + ")");
		
		LinkedList<int[]> ngRange = new LinkedList<int[]>();
		LinkedList<int[]> availableRange = new LinkedList<int[]>();
		LinkedList<int[]> yRange = new LinkedList<int[]>();
		//double velocity = (width + getScreenWidth()) * 1000 / (double) WavePlayer.SLIDE_IN_DURATION;
		//int secToCheck = (int) Math.ceil( velocity / width );
		
		// TODO fix me! use onScreenPVs instead of pdBuffer.
		
		// すべてのonScreenPVsに対して画面右端からの位置を計算。
		// 位置の値がwidthより小さい場合はNG。
		for( PostView pv :  onScreenPVs ){
			int delta = pv.getDisplayAt() - this.getElapsedTimeInSec();
			// see if distance - width < 0
			if(pv.getSlideInAnimationVelocity()*delta - width < 0){
				// {y-coord, 0:Invalid/1:Top/2:Bottom}
				int[] pointTop = {pv.getYCoord(), 1};
				int[] pointBottom = {pv.getYCoord()+pv.getHeight(), 2}; 
				ngRange.add(pointTop);
				//Log.e(TAG, " added: {" + pointTop[0] + "," + pointTop[1] + "}");
				ngRange.add(pointBottom);
				//Log.e(TAG, " added: {" + pointBottom[0] + "," + pointBottom[1] + "}");
			}
		}
		/*
		for(int i=1; i<4 ; i++){
			if( targetSec-i > 0 ){
				ArrayList<PostData> list = pdBuffer.get(targetSec-i);
				Log.e(TAG, " size of pdBuffer("+(targetSec-i)+"): " + list.size());
				for (PostData pd : list) {
					// see if X2 > 0
					if(pd.getWidth() - pd.getV()*i > 0){
						// {y-coord, 0:Invalid/1:Top/2:Bottom}
						int[] pointTop = {pd.getY(), 1};
						int[] pointBottom = {pd.getY()+pd.getHeight(), 2}; 
						ngRange.add(pointTop);
						ngRange.add(pointBottom);
					}
				}
			}
		}// outer for loop
		*/
		Log.e(TAG, " size of ngRange(past):" + ngRange.size());
		
		/*
		// check PostData in future
		for(int i=0; i<=secToCheck ; i++){
			if( i < durationInSec ){
				ArrayList<PostData> list = pdBuffer.get(targetSec+i);
				for (PostData pd : list) {
					// {y-coord, 0:Invalid/1:Top/2:Bottom}
					int[] pointTop = {pd.getY(), 1};
					int[] pointBottom = {pd.getY()+pd.getHeight(), 2}; 
					ngRange.add(pointTop);
					ngRange.add(pointBottom);
				}
			}
		}// outer for loop
		Log.e(TAG, " size of ngRange(future):" + ngRange.size());
		*/
		
		// sort points
		Collections.sort(ngRange, new Comparator<int[]>() {
			@Override
			public int compare(int[] lhs, int[] rhs) {
				return lhs[0] - rhs[0];
			}
	     });
		//Log.e(TAG, " ngRange sorted.");
		
		// flatten ngRange
		for(int i=0 ; i<ngRange.size() ; i++){
			int type = ngRange.get(i)[1];
			if(type == 0) continue;
			
			for(int j=i+1 ; j<ngRange.size() ; j++){	
				if( ngRange.get(j)[1]==type ){
					// mark as invalid 
					ngRange.get(j)[1]=0;
					// also mark pairing point as invalid
					for(int k=j+1 ; k<ngRange.size() ; k++){	
						if( ngRange.get(k)[1]!=0 && ngRange.get(k)[1]!=type ){
							ngRange.get(k)[1]=0;
							break;
						}
					}
				}else{
					// if point is different type, do nothing.
					break;
				}
			}
		}
		
		// remove invalid point
		for(int i=0 ; i<ngRange.size() ; i++){
			Log.e(TAG,"  ["+i+"] = (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
		}
		ArrayList<int[]> objToRemove = new ArrayList<int[]>();
		for(int i=0 ; i<ngRange.size() ; i++){
			Log.e(TAG," checking (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
			if( ngRange.get(i)[1]==0 ){
				Log.e(TAG," to remove " + i + " (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
				objToRemove.add(ngRange.get(i));
			}
		}
		
		for(int i=0 ; i<objToRemove.size() ; i++){
			int[] obj = objToRemove.get(i);
			ngRange.remove(obj);
			//Log.e(TAG," removed " + " (" + obj[0] + "," + obj[1] + ")");
		}
		
		// test
		for(int i=0 ; i<ngRange.size() ; i++){
			Log.e(TAG,"  ["+i+"] = (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
		}
		
		// remove too narrow range
		for(int i=0 ; i<ngRange.size() ; i++){
			int type = ngRange.get(i)[1];
			if(type == 0 || type == 1) continue;
			
			if(i+1<ngRange.size()){
				int delta = ngRange.get(i+1)[0] - ngRange.get(i)[0];
				if( delta<height ){
					// mark as invalid 
					ngRange.get(i)[1]=0;
					ngRange.get(i+1)[1]=0;
				}
			}
		}
		// remove invalid point
		for(int i=0 ; i<ngRange.size() ; i++){
			Log.e(TAG,"  ["+i+"] = (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
		}
		objToRemove = new ArrayList<int[]>();
		for(int i=0 ; i<ngRange.size() ; i++){
			Log.e(TAG," checking (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
			if( ngRange.get(i)[1]==0 ){
				Log.e(TAG," to remove " + i + " (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
				objToRemove.add(ngRange.get(i));
			}
		}
		for(int i=0 ; i<objToRemove.size() ; i++){
			int[] obj = objToRemove.get(i);
			ngRange.remove(obj);
			//Log.e(TAG," removed " + " (" + obj[0] + "," + obj[1] + ")");
		}
		
		// test
		for(int i=0 ; i<ngRange.size() ; i++){
			Log.e(TAG,"  final ng ["+i+"] = (" + ngRange.get(i)[0] + "," + ngRange.get(i)[1] + ")");
		}
		
		// calculate availableRange
		// {y-coord, 0:Invalid/1:Top/2:Bottom}
		int[] firstPoint = {0, 1};
		int[] lastPoint = {(int) Helpers.convertPixelsToDp(screen.getHeight(), activity), 2};
		availableRange.add(firstPoint);
		availableRange.add(lastPoint);
		for(int i=0 ; i<ngRange.size() ; i++){
			int[] point = ngRange.get(i);
			point[1] = (point[1]==1) ? 2 : 1 ;
			availableRange.add(point);
		}
		
		// sort points
		Collections.sort(availableRange, new Comparator<int[]>() {
			@Override
			public int compare(int[] lhs, int[] rhs) {
				return lhs[0] - rhs[0];
			}
	     });
		
		// test
		for(int i=0 ; i<availableRange.size() ; i++){
			Log.e(TAG,"  final availableRange ["+i+"] = (" + availableRange.get(i)[0] + "," + availableRange.get(i)[1] + ")");
		}
		
		// calculate Y Range
		for(int i=0 ; i<availableRange.size() ; i++){

			int type = availableRange.get(i)[1];
			if(type == 0 || type == 1) continue;
			
			if( 0<i-1 ){
				int[] bottomPoint = availableRange.get(i);
				int[] topPoint = availableRange.get(i-1);
				int delta = bottomPoint[0] - topPoint[0] - height;
				//Log.e(TAG,"  bottomPoint:"+bottomPoint[0]+" topPoint:" + topPoint[0] + " : delta " + height);
				if( delta >= 0 ){
					int[] newBottom = {bottomPoint[0] - height, 2};
					yRange.add(topPoint);
					yRange.add(newBottom);
				}
			}
			
		}

		// test
		for(int i=0 ; i<yRange.size() ; i++){
			Log.e(TAG,"  final yRange ["+i+"] = (" + yRange.get(i)[0] + "," + yRange.get(i)[1] + ")");
		}
		
		int y;
		Random random = new Random();
		if( yRange.size()>0 ){
			// choose range first
			int numOfRange = yRange.size()/2;
			int randomIndex = random.nextInt(numOfRange)*2;
			// choose randam y
			int[] topPoint = yRange.get(randomIndex);
			int[] bottomPoint = yRange.get(randomIndex+1);
			y = random.nextInt(bottomPoint[0] + 1 - topPoint[0]) + topPoint[0];
		}else{
			// get completely random Y from screen
			y = random.nextInt((int) Helpers.convertPixelsToDp(screen.getHeight(), activity) - height);
		}
		Log.e(TAG,"  Y:" + y);
		
		return y;
	}
	
	/*
	public boolean isControlVisible(){
		return control.isShown();
	}
	

	public void extendHideControlTimeout(){
		hideControlTimeout = 2000;
	}
	
	public void cancelHideControlAnimation(){
		if(hideControlAnimationTimerTask != null){
			hideControlAnimationTimerTask.cancel();
			hideControlAnimationTimerTask = null;
		}
	}
	
	public boolean isHideControlAnimationRunning(){
		return isHideControlAnimationRunning;
	}
	
	// to extend counting, call extendHideControlTimeout()
	public void startCountForHideControl(){
		
	    final int INTERVAL = 1000;

	    hideControlAnimationTimerTask = new TimerTask(){
	    	long elapsed;
            @Override
            public void run() {
                elapsed += INTERVAL;
                if(elapsed >= hideControlTimeout){
                    this.cancel();
                    Log.i(TAG,"setAnimation(hideControlAnimation)");
                    control.clearAnimation();
                    hideControlAnimation = new HideControlAnimation();
            		hideControlAnimation.setInterpolator(new LinearInterpolator());
            		hideControlAnimation.setDuration(600);
            		hideControlAnimation.setAnimationListener(hideControlAnimationListener);
            		control.setAnimation(hideControlAnimation);
                    return;
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(hideControlAnimationTimerTask, INTERVAL, INTERVAL);
	}

	public void showControl(){
		control.clearAnimation();
		showControlAnimation = new ShowControlAnimation();
		showControlAnimation.setInterpolator(new LinearInterpolator());
		showControlAnimation.setDuration(0);
		showControlAnimation.setAnimationListener(showControlAnimationListener);
		control.setAnimation(showControlAnimation);
	}
	*/
	
	public int getDurationInSec(){
		return this.durationInSec;
	}

	public int getElapsedTimeInSec(){
		return (int) getCurrentPosition()/1000;
	}
	
	public int getScreenWidth() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) activity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		
		return displayMetrics.widthPixels;
	}
	
	public Activity getActivity(){
		return this.activity;
	}
	
	public boolean isPrepared(){
		return isPlayerPrepared && isDataPrepared;
	}
	
	public String getMediaId(){
		return mediaId;
	}
	
	@Override
	public void seekTo(int msec){
		Log.d (TAG, "SeekTo()");
		
		super.seekTo(msec);
	}
	
	private void stopUpdatingSeekBar(){
		isUpdatingSeekBar = false;
	}
	
	private void startUpdatingSeekBar(){
		isUpdatingSeekBar = true;
	}
	
	private void stopUpdatingElapsedText(){
		isUpdatingElapsedText = false;
	}
	
	private void startUpdatingElapsedText(){
		isUpdatingElapsedText = true;
	}
	
	/*
	 * MediaPlayerListeners
	 */
	private MediaPlayer.OnBufferingUpdateListener mBufferingListener = new MediaPlayer.OnBufferingUpdateListener() {
		
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {

			if( isUpdatingElapsedText ) elapsedTextView.setText(Helpers.formatMilliseconds(mp.getCurrentPosition()));
			
			if( isPlaying() ){
				final int elapsedInSec = (int) mp.getCurrentPosition()/1000;
				//Log.d (TAG, "current position:" + elapsedInSec + "/" + durationInSec);
				
				// update user interface
				if( isUpdatingElapsedText ) elapsedTextView.setText(Helpers.formatMilliseconds(mp.getCurrentPosition()));
				if( isUpdatingSeekBar ) seekBar.setProgress( (int) (((double) elapsedInSec/ (double) durationInSec) * 100) );
				
				// do heavy lifting stuff in new thread
				new Thread(new Runnable() {
			        public void run() {
						displayPostViews(elapsedInSec);
			        }
			    }).start();
				
				// update buffering progress
				seekBar.setSecondaryProgress(percent);
				
			}// end of isPlaying()
			
		}// end of OnBufferingUpdate()
		
	};
	
	private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		/** Called when MediaPlayer is ready **/
		public void onPrepared(MediaPlayer player) {			
			Log.d (TAG, "onPrepared called.");
			
			isPlayerPrepared = true;
			durationInSec = (int) player.getDuration()/1000;
			durationTextView.setText(Helpers.formatMilliseconds(player.getDuration()));
			
			startPlayer();
		}
	};
	
	private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			Log.d (TAG, "onInfo: " + what + extra);
			
			switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					showLoading();
					pauseAnimation();
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					hideLoading();
					resumeAnimation();
					break;
			}
			
			return true;
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {        
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			//TODO sometimes this happens for some reason...
	        // The MediaPlayer has moved to the Error state, must be reset!
			Log.d (TAG, "onError: " + framework_err + impl_err);
			
			Toast.makeText(activity, "エラーが発生しました。", Toast.LENGTH_SHORT).show();
			hideLoading();
			
			// disable touch event on screen
			screen.setFocusable(false);
			screen.setEnabled(false);
			
			//release();
			return true;
		}
	};
	
	/*
	 * SeekBar Listeners
	 */
	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener(){
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			Log.d (TAG, "onProgressChanged()");
			
			if(fromUser){
				//progress to elapsed time
				int elapsedInMillisec = (int) ((double) progress * (double) durationInSec * 10);
				elapsedTextView.setText(Helpers.formatMilliseconds(elapsedInMillisec));
			}
	    }
		
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.d (TAG, "onStartTrackingTouch()");
			
			stopUpdatingSeekBar();
			stopUpdatingElapsedText();
			
			// stop blinking elapsed text
			elapsedTextView.clearAnimation();
        }
		
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.d (TAG, "onStopTrackingTouch()");
			
			pausePlayer();
			isDataPrepared = false;
				
			// remove all PostViews from screen and empty buffer.
			pdBuffer.stopPushingData();

			ArrayList<PostView> listToReset = new ArrayList<PostView>();
			for (PostView pv : onScreenPVs) {
				listToReset.add(pv);
			}
			for (PostView pv : listToReset) {
				pv.cancelAnimation();
			}
		
			int seekToMSec = (int) ((double) durationInSec * (double) seekBar.getProgress()) * 10;

			startPositionInSec = seekToMSec/1000;			
			// we need 5sec of past data to display. 
			int targetMin = (startPositionInSec-5 >= 0) ? ((startPositionInSec-5)/60)+1 : 0;
			postDataReader.setNextMin(targetMin);
			
			seekTo(seekToMSec);
			
			/**
			if( ! isPlaying() ) {
				// keep showing control and start blinking elapsed text.
				Animation anim = new AlphaAnimation(0.0f, 1.0f);
				anim.setDuration(50);
				anim.setStartOffset(500);
				anim.setRepeatMode(Animation.REVERSE);
				anim.setRepeatCount(Animation.INFINITE);
				elapsedTextView.startAnimation(anim);
			}
			*/
        }
		
	};
	
	private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			Log.d (TAG, "onSeekComplete()");
			startPlayer();
		}
		
	};
	
	/*
	 * SlideInAnimationListeners
	 */
	private SlideInAnimation.AnimationListener slideInAnimationListener  = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) { 
        	//Log.e (TAG,"slideInAnimation: onAnimationStart");
        	SlideInAnimation anim = (SlideInAnimation) animation;
        	PostView pv = anim.getPostView();
        	if( displayedAtSeekPVs.contains(pv) ){
        		pv.animationMove();
        		displayedAtSeekPVs.remove(pv);
        	}
        	
        }

        @Override
        public void onAnimationRepeat(Animation animation) { }

        @Override
        public void onAnimationEnd(Animation animation) {
        	//Log.d (TAG,"onAnimationEnd");
  	
    		SlideInAnimation a = (SlideInAnimation) animation;
    		PostView pv = a.getPostView();
    		pv.setVisibility(View.INVISIBLE);
    		onScreenPVs.remove(pv);
    		addToFreeQueue(pv);
        }
    };
    
	/*
	 * NewPostAnimationListeners
	 */
	private NewPostAnimation.AnimationListener newPostAnimationListener  = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        	//Log.e (TAG,"newPostAnimation: onAnimationStart isAnimationRunning:" + isAnimationRunning);
        	((NewPostAnimation) animation).onAnimationStart();
        	//a.onAnimationStart();
	    	
        }

        @Override
        public void onAnimationRepeat(Animation animation) { }

        @Override
        public void onAnimationEnd(Animation animation) {
        	//Log.d (TAG,"onAnimationEnd");
  	
    		NewPostAnimation a = (NewPostAnimation) animation;
    		PostView pv = a.getPostView();
    		pv.setVisibility(View.INVISIBLE);
    		onScreenPVs.remove(pv);
    		addToFreeQueue(pv);
        }
    };
    
    private PostDataBufferListener mPostDataBufferListener = new PostDataBufferListener(){

		@Override
		public void onPostDataBufferReady(final int fromSec) {
			Log.e (TAG, "onPostDataBufferReady("+fromSec+")");
			
			activity.runOnUiThread(new Runnable() {
        		public void run() {
        			// display PVs for past 4 sec
        			for ( int i = 4; i > 0; i-- ) {
        				int sec = startPositionInSec - i;
        				if( sec >= 0 ){
        					final ArrayList<PostData> listToBeDisplayed = pdBuffer.getPdListAt(sec);
        					if(listToBeDisplayed != null){
        				    			
				    			// set PostData to PostView
				    			while(listToBeDisplayed.size() > 0){
					    			if(freePVs.size() > 0){
					    				PostData myData = listToBeDisplayed.remove(0);
					    				PostView pv = freePVs.remove(0);
					    				String color = (userName==myData.getUserName()) ? "yellow" : "white";
					    				pv.setData(myData.getText(), myData.getThumbData(), myData.getUserName(), myData.getCreatedDate(), myData.getCreatedTime(), myData.getY(), myData.getDisplayAt(), color, myData.getSlideInAnimationVerocity());
						    			pv.startSlideIn();
						    			pv.animationSetTimeToMove(i*1000);
								    	onScreenPVs.add(pv);
								    	displayedAtSeekPVs.add(pv);
					    			}else{
					    				Log.e (TAG, "No free PostView instance!!");
					    			}
				    			} // end of while loop	
        					}// end of if stmt
        				}
        			} // end of for loop
        			
        			isDataPrepared = true;
        			startPlayer();
        		}
    		}); // end of runOnUiThread
		} 
    	
    };
    
    /*
     * dataBufferingListeners
     */
    /*
    private DataBufferingListener dataBufferingListener = new DataBufferingListener(){
    	public void onBufferingStart(MediaPlayer mp){
    		Log.i (TAG,"onBufferingStart()");
    		
			isDataBuffering = true;
    		mp.pause();
    		showLoading();
			pauseAnimation();
    	}
    	public void onBufferingEnd(MediaPlayer mp){
    		Log.i (TAG,"onBufferingEnd()");
    		
    		isDataBuffering = false;
    		mp.start();
    		hideLoading();
    		resumeAnimation();
    	}
    };
    public interface DataBufferingListener {
    	public void onBufferingStart(MediaPlayer mp);
    	public void onBufferingEnd(MediaPlayer mp);
    }
    */
    
    private PostDataReaderListener mPostDataReaderListener = new PostDataReaderListener(){
    	
		@Override
		public void onGetPostDataUpdate(int min) {
			Log.d (TAG, "onGetPostDataComplete("+min+")");
			
			int endSec = (60*min)-1;
			Log.d(TAG, " min:" + min + " endSec:" + endSec + " startPositionInSec:" + startPositionInSec + " isPushingStarted:" + pdBuffer.isPushingStarted());
			if( endSec - startPositionInSec >= PostDataBuffer.INITIAL_BUFFER_SIZE && ! pdBuffer.isPushingStarted()){
				pdBuffer.startPushingData(startPositionInSec);
			}
            
		}

		@Override
		public void onError() {
			// TODO Auto-generated method stub
			
		}
    };
    
    private RelativeLayout.OnTouchListener mScreenTouchListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.d (TAG, "onTouch()");
			
			if( event.getAction() == MotionEvent.ACTION_DOWN ){
				togglePlayPause();
			}
			return false;
		}
    	
    };
    
    private RestProcessor.RestProcessorListener mRestProcessorListener = new RestProcessorListener(){

		@Override
		public void onGetPostDataComplete(int min) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCreatePostDataComplete(String uuid) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError() {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    private View.OnClickListener mPostButtonOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			// create PostData
			int targetDisplayTime = getElapsedTimeInSec();
			PostData postData = new PostData(v.getContext(), 0, getScreenWidth(), targetDisplayTime, postEditText.getText().toString(), userThumbData, userName, "Apr12 '13", "10:25AM");
			int y = calculateYForNewPost( targetDisplayTime, postData.getWidth(), postData.getHeight() );
			postData.setY(y);
			
			// save postdata to sqlite
			String userThumbString = Base64.encodeToString(userThumbData, Base64.DEFAULT);
			WaveSQLiteHelper waveSQLiteHelper = new WaveSQLiteHelper(activity);
			waveSQLiteHelper.savePost(y, postData.getDisplayAt(), postData.getText(), userThumbString, userName, postData.getCreatedDate(), postData.getCreatedTime());
			
			// TODO REST
			String[] sampleUserIds = {"56656680-ad49-11e2-9431-1deab5f008b7","bdbe9d60-ad49-11e2-9431-1deab5f008b7","961591b0-ad49-11e2-9431-1deab5f008b7",
					"aec9ba10-ad49-11e2-9431-1deab5f008b7", "62aa6c10-ad49-11e2-9431-1deab5f008b7","78331e10-ad49-11e2-9431-1deab5f008b7",
					"c6c2a8c0-ad49-11e2-9431-1deab5f008b7","d6a21320-ad49-11e2-9431-1deab5f008b7"};
			Random random = new Random();
			int randomNumber = random.nextInt(7);
			
			UUID uuid = UUID.randomUUID();
			String uuidString = uuid.toString();
			
			if(freePVs.size() > 0){
				// retrieve available PostView instance for display.
				PostView newPV = freePVs.remove(0);				
				newPV.setData(postData.getText(), postData.getThumbData(), postData.getUserName(), 
						postData.getCreatedDate(), postData.getCreatedTime(), postData.getY(), postData.getDisplayAt(), "yellow", postData.getSlideInAnimationVerocity());
		    	//newPV.startNewPostAnimation();
				if( isAnimationRunning ){
					newPV.startNewPostAnimation();
				}else{
					newPV.pauseAfterStartNewPostAnimation();
				}
		    	onScreenPVs.add(newPV);

			}
			
//			restProcessor.createPostData(mediaId, sampleUserIds[randomNumber], uuidString, pd);
			postEditText.setText("");
			
			InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
		}
	};
    
	
	////
	//
	//  Class Variables
	//
	////	
    private static final String TAG = "WavePlayer";
    private static final int NUM_OF_PV_INSTANCE = 30; 	// Number of PostView instances. Instances will be reused. 5views/sec*8sec=40
    public static final int SLIDE_IN_DURATION = 5000;  // duration of SlideInAnimation
    public static final int NEW_POST_DURATION = 4000;  // duration of NewPostAnimation
    
    private PostDataBuffer pdBuffer; 					// holds PostData sent from server.
    private ArrayList<PostView> freePVs;  				// Array of PostView instance that can be used
    private HashSet<PostView> onScreenPVs; 				// PVs currently displayed
    private HashSet<PostView> displayedAtSeekPVs; 		// PVs displayed on screen when seeked
    
    private int durationInSec = 0;
    private int startPositionInSec = 0; // set when start retrieving data
    private String mediaId;
    private String userName;
    private byte[] userThumbData;
    private PostDataReader postDataReader;
    private RestProcessor restProcessor;
    
    private Activity activity;
    private RelativeLayout screen;
    private RelativeLayout control;
    private ProgressBar loadingProgressBar;
    private SeekBar seekBar;
    private TextView durationTextView;
    private TextView elapsedTextView;
    private EditText postEditText;
    private Button postButton;
    private boolean isPlayerPrepared = false;
    private boolean isDataPrepared = false;
    private boolean isAnimationRunning = false;
    //private boolean isDataBuffering = false;
    private boolean isUpdatingSeekBar = true;
    private boolean isUpdatingElapsedText = true;

}
