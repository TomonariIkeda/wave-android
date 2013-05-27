package jp.cleartouch.wave;

import android.util.Log;
import jp.cleartouch.libs.rest.RestProcessor;
import jp.cleartouch.libs.rest.RestProcessor.RestProcessorListener;

public class PostDataReader {

	public PostDataReader(WavePlayer wp){
		this.wavePlayer = wp;
		this.restProcessor = new RestProcessor(wp);
		this.restProcessor.setOnRestProcessorListener(mRestProcessorListener);
	}
	
	public void startRetrievingPostData(int min) {
		Log.d(TAG, "startRetrievingPostData(" + min + ")");
		
		int startingSec = 60*(min-1);
		int duration = wavePlayer.getDurationInSec(); // duration is 0 before player gets ready.

		if(duration == 0 || 
				0 < min && startingSec < duration){
			currentMin = min;
			restProcessor.getPostData(wavePlayer.getMediaId(), currentMin);
		}else{
			onPostDataReaderListener.onGetPostDataUpdate(min);
		}
		
	}
	
	/*
	 * data at min will be retrieved from next cycle.
	 */
	public void setNextMin(int min){
		
		if( reachedEndOfMedia ){
			// already reached the end of media. need to restart operation again.
			reachedEndOfMedia = false;
			currentMin = min;
			restProcessor.getPostData(wavePlayer.getMediaId(), currentMin);
		}else{
			// retrieve data at next cycle
			currentMin = min - 1;
		}
	}
	
	private RestProcessorListener mRestProcessorListener = new RestProcessorListener(){

		@Override
		public void onGetPostDataComplete(int min) {
		
			// notify via callback
			onPostDataReaderListener.onGetPostDataUpdate(min);
			
			// retrieve next PostData chunk
			int toSec = (60*min)-1;
			if( toSec < wavePlayer.getDurationInSec()-1){
				currentMin++;
				restProcessor.getPostData(wavePlayer.getMediaId(), currentMin);
			}else{
				// reached end of media
				reachedEndOfMedia = true;
			}
  
		}

		@Override
		public void onError() { }

		@Override
		public void onCreatePostDataComplete(String uuid) { }

		@Override
		public void onGetPostCountComplete() { }

		@Override
		public void onUpdatePostCountComplete(int count_at) { }
		
	};
	
	////
	//
	//  Callbacks
	//
	////	
	public void setOnPostDataReaderListener(PostDataReaderListener mPostDataReaderListener) {
		this.onPostDataReaderListener = mPostDataReaderListener;
	}
	
    public interface PostDataReaderListener {
    	public void onGetPostDataUpdate(int min);
    	public void onError();
    }
	    
	////
	//
	//  Class Variables
	//
	////
	private static final String TAG = "PostDataReader";
	private int currentMin;  // minute of currently retrieving PostData. must be in range of 0 to duration.
	private boolean reachedEndOfMedia = false; // if already reached the end of media and finished retrieving data.
	
	private WavePlayer wavePlayer;
	private RestProcessor restProcessor;
	private PostDataReaderListener onPostDataReaderListener;
}
