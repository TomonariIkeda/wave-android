package jp.cleartouch.postcast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.LocalServerSocket;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class CasterPlayer extends MediaPlayer{

	public CasterPlayer(Activity activity, RelativeLayout screen, RelativeLayout control, SeekBar seekBar, 
							TextView elapsed, TextView duration, ProgressBar loading){
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
		
		// setup PostData buffer and displayQueue
		pdBuffer = new SparseArray<ArrayList<PostData>>();
		displayQueue = new ArrayList<PostView>();
		freePVs = new ArrayList<PostView>();
		onScreenPVs = new HashSet<PostView>();
		
		// register listeners
		this.setOnPreparedListener(mPreparedListener);
		this.setOnInfoListener(mInfoListener);
		this.setOnErrorListener(mErrorListener);
		this.setOnBufferingUpdateListener(mBufferingListener);
		this.seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		this.screen.setOnTouchListener(mScreenTouchListener);
		
		// control animation
		hideControlAnimation = new HideControlAnimation();
		hideControlAnimation.setInterpolator(new LinearInterpolator());
		hideControlAnimation.setDuration(700);
		hideControlAnimation.setAnimationListener(hideControlAnimationListener);
		startCountForHideControl();
		
		
		// instantiate 11 postviews and put it in queue
		PostView tmp;
		for ( int i = 0; i < NUM_OF_PV_INSTANCE+1; ++i ) {
			tmp = new PostView(activity);
			screen.addView(tmp);
			
			// set animation listener
			SlideInAnimation animation = tmp.getSlideInAnimation();
	        animation.setAnimationListener(slideInAnimationListener);
			addToFreeQueue(tmp);
		}// end of for loop
		
	}
	
	private JSONObject parseJsonFileToJavaObjects(String json){
		Log.d (TAG,json);
		
		JSONObject jObj = null;
		
		// try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        
        return jObj;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.media.MediaPlayer#setDataSource(java.lang.String)
	 */
	public void setDataSource(String audio_path, String data_path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException{
		
		getPostDataAt(0);
		
		super.setDataSource(audio_path);
		super.prepareAsync();
	}
	
	/*
	 * get PostData from server.
	 * PostDatas from elapsed(sec) to elapsed+POSTDATA_BUFFER_SIZE(sec)
	 * will be retrieved.
	 */
	private void getPostDataAt(int elapsed){
		// get JSON locally
		AssetManager manager = activity.getAssets();
        InputStream file;
        String jsonString = "";
		try {
			file = manager.open("data.json");
			byte[] data = new byte[file.available()];
	        file.read(data);
	        file.close();
	        jsonString = new String(data);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        JSONObject json = parseJsonFileToJavaObjects(jsonString);
        JSONArray posts = null;
        try {
            // Getting Array of Contacts
            posts = json.getJSONArray("posts");
         
            // looping through All Contacts
            for(int i = 0; i < posts.length(); i++){
                JSONObject c = posts.getJSONObject(i);
         
                // Storing each json item in variable
                String userName = c.getString("userName");
                int thumb = c.getInt("thumb");
                String dataString = c.getString("data");
                int type = c.getInt("type");
                int yCoord = c.getInt("y");
                int postedAt = c.getInt("postedAt");
                int createdAt = c.getInt("createdAt");
         
        		ArrayList<PostData> pdList = new ArrayList<PostData>();
        		pdList.add(new PostData(yCoord, postedAt, dataString, thumb, userName, "Apr12 '13", "10:25AM"));
        		writePdList(postedAt, pdList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        startReceivingPostData(elapsed, 10);
	}
	
	/*
	 * start receiving PostData from server via WebSocket.
	 */
	public void startReceivingPostData(int sec, int buffer_size){
		
		// Create WebSocket
        Thread mLooper = new Thread(){
        	public void run(){
        		Log.i(TAG, "Thread mLooper started!");
        		/*
		        try {
		        	
		        	if(lss!=null) lss.close();
		            lss = new LocalServerSocket(SOCKET_NAME);
		            // blocks until new connection arrives.
		            receiver = lss.accept(); 
		            Log.i(TAG, "LocalServerSocket created.");
		            
		            // socket connected! get file descriptor.
		    		fd = receiver.getFileDescriptor();
		    		Log.i(TAG, "get file descriptor.");
		    		
		    		nativeInit(fd);
		    		
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        */

        	}
        };
        mLooper.start();
		
		// check buffering
	}
	
	/*
	 * writePdList is called from Websocket client to supply
	 * PostData recieved from server.
	 * 
	 * pd_list: List of PostData at given sec
	 * 
	 */
	public void writePdList(int sec, ArrayList<PostData> pd_list){
		pdBuffer.append(sec, pd_list);
	}
	
	public ArrayList<PostData> getPdList(int sec){
		Log.d (TAG, "getPdList()");
		
		ArrayList<PostData> list = pdBuffer.get(sec);
		if( list != null ){
			pdBuffer.delete(sec);
		}
		return list;
	}
	
	/*
	 * Called from OnBufferingUpdateListener() repeatedly
	 * when CasterPlayer is in playing state. displayPV() will be called right after.
	 * 
	 */
	public void setDisplayQueue(int sec){
		Log.d (TAG, "setDisplayQueue()");
		
		final ArrayList<PostData> listToBeDisplayed = getPdList(sec);	
		// listToBeDisplayed set to null when there is no PostData at given sec.
		if(listToBeDisplayed != null){
			
			activity.runOnUiThread(new Runnable() {
	    		public void run() {
	    			
	    			// set PostData to PostView
	    			while(listToBeDisplayed.size() > 0){
		    			if(freePVs.size() > 0){
		    				PostData myData = listToBeDisplayed.remove(0);
		    				// retrieve available PostView instance for display.
		    				PostView nextPV = freePVs.remove(0);
		    				nextPV.setData(myData.getText(), myData.getThumbResId(), myData.getUserName(), myData.getCreatedDate(), myData.getCreatedTime(), myData.getY(), myData.getPostedAt());
			    			
					    	displayQueue.add(nextPV);
		    			}else{
		    				Log.e (TAG, "No free PostView instance!!");
		    			}
	    			} // end of while loop
	    			
	    		}// end of run()
			});// end of runOnUiThread()
			
		}// end of if stmt
			
	}
	
	// Called from OnBufferingUpdateListener() repeatedly.
	public void displayPV(){
		Log.d (TAG, "displayPV()");
		
		activity.runOnUiThread(new Runnable() {
			PostView pv;
    		public void run() {
    			// display all queued PostViews. 
				for ( int i = 0; i < displayQueue.size(); ++i ) {
					pv = displayQueue.remove(i);
					pv.startSlideIn();
					Log.d (TAG, pv.getContentText() + " started.");
				}
    		}
		});// end of runOnUiThread()
	}
	
	public void pauseAnimation(){
		Iterator<PostView> it = onScreenPVs.iterator();
        while (it.hasNext()) {
            it.next().pauseAnimation();
        }
	}
	
	public void resumeAnimation(){
		Iterator<PostView> it = onScreenPVs.iterator();
        while (it.hasNext()) {
            it.next().resumeAnimation();
        }
	}	
	
	public void addToFreeQueue(PostView pv){
		Log.d (TAG, "addToFreeQueue()");
		freePVs.add(pv);
	}
	
	public void startPlayer(){
		if(isPrepared()){
			super.start();
			resumeAnimation();
			
			// stop blinking
			elapsedTextView.clearAnimation();
		}
	}
	
	public void pausePlayer(){
		if(isPrepared()){
			super.pause();
			pauseAnimation();
			
			// start blinking
			Animation anim = new AlphaAnimation(0.0f, 1.0f);
			anim.setDuration(50); //You can manage the time of the blink with this parameter
			anim.setStartOffset(500);
			anim.setRepeatMode(Animation.REVERSE);
			anim.setRepeatCount(Animation.INFINITE);
			elapsedTextView.startAnimation(anim);
			
		}
	}
	
	public void togglePlayPause(){

		if(isPlaying()){
			pausePlayer();
			cancelHideControlAnimation();
		}else{
			startPlayer();
			startCountForHideControl();
		}
	}
	
	public void release(){
		super.release();
	}
	
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
	
	public void showLoading(){
		loadingProgressBar.setVisibility(ProgressBar.VISIBLE);
		loadingProgressBar.bringToFront();
	}
	
	public void hideLoading(){
		loadingProgressBar.setVisibility(ProgressBar.INVISIBLE);
	}
	
	public boolean isPrepared(){
		return isPrepared;
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
			//Log.i ("Buffering", "" + percent);
			
			int elapsedInSec = (int) mp.getCurrentPosition()/1000;
			Log.d (TAG, "current position:" + elapsedInSec + "/" + durationInSec);
			
			// update buffering progress
			seekBar.setSecondaryProgress(percent);
			
			if(isPlaying()){
				setDisplayQueue(elapsedInSec);
				displayPV();
				if(isUpdatingElapsedText) elapsedTextView.setText(Helpers.formatMilliseconds(mp.getCurrentPosition()));
				if(isUpdatingSeekBar) seekBar.setProgress( (int) (((double) elapsedInSec/ (double) durationInSec) * 100) );
			}
		}
	};
	
	private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		/** Called when MediaPlayer is ready **/
		public void onPrepared(MediaPlayer player) {			
			Log.d (TAG, "onPrepared called.");
			
			isPrepared = true;
			durationInSec = (int) player.getDuration()/1000;
			durationTextView.setText(Helpers.formatMilliseconds(player.getDuration()));
			
			hideLoading();
			player.start();
			
			// enable EditText
			/*
			EditText et = (EditText) activity.findViewById(R.id.play_edit_text);
			et.setEnabled(true);
			et.setFocusable(true);
			et.requestFocus();
			*/
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
			// ... react appropriately ...
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
			cancelHideControlAnimation();
			
			// stop blinking elapsed text
			elapsedTextView.clearAnimation();
        }
		
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.d (TAG, "onStopTrackingTouch()");
			
			seekTo((int) ((double) durationInSec * (double) seekBar.getProgress()) * 10);
			startUpdatingSeekBar();
			startUpdatingElapsedText();
			if(isPlaying()) {
				// hide control if isPlaying() when seek ends.
				startCountForHideControl();
			}else{
				// keep showing control and start blinking elapsed text.
				Animation anim = new AlphaAnimation(0.0f, 1.0f);
				anim.setDuration(50);
				anim.setStartOffset(500);
				anim.setRepeatMode(Animation.REVERSE);
				anim.setRepeatCount(Animation.INFINITE);
				elapsedTextView.startAnimation(anim);
			}
        }
		
	};

	/*
	 * ShowControlAnimation Listeners
	 */
	private ShowControlAnimation.AnimationListener showControlAnimationListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        	// enable control
        	seekBar.setEnabled(true);
    		activity.runOnUiThread(new Runnable() {
        		public void run() {
        			control.setVisibility(View.VISIBLE);
        		}
    		});
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {}
	};	
	
	/*
	 * HideControlAnimation Listeners
	 */
	private HideControlAnimation.AnimationListener hideControlAnimationListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        	seekBar.setEnabled(false);
        	isHideControlAnimationRunning = true;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
    		isHideControlAnimationRunning = false;
    		activity.runOnUiThread(new Runnable() {
        		public void run() {
        			control.setVisibility(View.INVISIBLE);
        		}
    		});
        }
	};
	
	/*
	 * SlideInAnimationListeners
	 */
	private SlideInAnimation.AnimationListener slideInAnimationListener  = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) { 
        	//Log.d (TAG,"onAnimationStart");
        	
        	SlideInAnimation a = (SlideInAnimation) animation;
    		PostView pv = a.getPostView();
        	onScreenPVs.add(pv);
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
     * dataBufferingListeners
     */
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
    
    private RelativeLayout.OnTouchListener mScreenTouchListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.d (TAG, "onTouch()");
			
			if(event.getAction() == MotionEvent.ACTION_DOWN 
					&& ! isHideControlAnimationRunning()){
				if(isControlVisible()) {
					togglePlayPause();
				}else{
					showControl();
					startCountForHideControl();
				}
			}
			return false;
		}
    	
    };
    
    private static final String TAG = "CasterPlayer";
    private static final int NUM_OF_PV_INSTANCE = 11; 	// Number of PostView instances. Instances will be reused.
    private static final int POSTDATA_BUFFER_SIZE = 5;
 
    private SparseArray<ArrayList<PostData>> pdBuffer; 	// holds PostData sent from server.
    private ArrayList<PostView> freePVs;  				// Array of PostView instance that can be used
    private HashSet<PostView> onScreenPVs; 				// PVs currently displayed
    private ArrayList<PostView> displayQueue; 			// Array of PostView that will be displayed at next cycle
    
    private int durationInSec = 0;
    private int hideControlTimeout = 2000;
    
    private Activity activity;
    private RelativeLayout screen;
    private RelativeLayout control;
    private ProgressBar loadingProgressBar;
    private SeekBar seekBar;
    private TextView durationTextView;
    private TextView elapsedTextView;
    private HideControlAnimation hideControlAnimation;
    private TimerTask hideControlAnimationTimerTask = null;
    private ShowControlAnimation showControlAnimation;
    private boolean isPrepared = false;
    private boolean isDataBuffering = false;
    private boolean isUpdatingSeekBar = true;
    private boolean isUpdatingElapsedText = true;
    private boolean isHideControlAnimationRunning = false;
}
