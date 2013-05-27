package jp.cleartouch.libs.rest;

import jp.cleartouch.libs.rest.RestClient.RestCompleteListener;
import jp.cleartouch.sqlite.WaveSQLiteHelper;
import jp.cleartouch.wave.Indicator;
import jp.cleartouch.wave.PostData;
import jp.cleartouch.wave.WavePlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

public class RestProcessor {
	
	public RestProcessor(WavePlayer wp){
		
		this.wavePlayer = wp;
		waveSQLiteHelper = new WaveSQLiteHelper(wavePlayer.getActivity());
		restClient = new RestClient();
		restClient.setOnRestCompleteListener(mRestCompleteListener);
	}

	////
	//
	//  GET PostData
	//
	////
	public void getPostData(final String mediaId, final int min){
		Log.d (TAG, "getPostData("+min+")");
		
		if(min < 0){
			onRestProcessorListener.onError();
			return;
		}
			
		Integer status = waveSQLiteHelper.getGetPostRequestStatus(min);
		
		if(status == null){
			
			// request does not exist, save request data and make request
			waveSQLiteHelper.saveGetPostRequest(min, RestClient.REST_REQUEST_STATUS_REQUESTING);
			int fromSec = 60*(min-1);
			int toSec = (60*min)-1;
			restClient.getPostData(mediaId, fromSec, toSec);
			
		}else if(status.intValue() == RestClient.REST_REQUEST_STATUS_COMPLETE){
			
			// no need to make request, notify via callback
			onRestProcessorListener.onGetPostDataComplete(min);
			
		}else if(status.intValue() == RestClient.REST_REQUEST_STATUS_REQUESTING){
			
			// request in progress. wait for 2sec and check again.
			new Handler().postDelayed(new Runnable()
			{
			    @Override
			    public void run()
			    {
			    	getPostData(mediaId, min);
			    }
			}, 2000);
			
		}else if(status.intValue() == RestClient.REST_REQUEST_STATUS_ERROR){
			// request returned with error.
			onRestProcessorListener.onError();
		}

	}
	
	private void savePostsToSQLite(JSONArray posts, int fromSec, int toSec) {
		//Log.d (TAG, "addPosts()");
		
        try {
            
            // looping through All PostData and add to DB
            for(int i = 0; i < posts.length(); i++){
                JSONObject c = posts.getJSONObject(i);
         
                // Storing each json item in variable
                String userName = c.getString("user_name");
                String thumb = c.getString("thumb");
                String dataString = c.getString("comment");
                int type = c.getInt("type");
                int y = c.getInt("y");
                int displayAt = c.getInt("display_at");
                long createdAt = c.getLong("created_at");
                
                waveSQLiteHelper.savePost(type, y, displayAt, dataString, thumb, userName, createdAt);
            }

        } catch (JSONException e) {
        	Log.e(TAG, e.toString());
        	onRestProcessorListener.onError();
        }
	}

	////
	//
	//  POST PostData
	//
	////
	public void createPostData(final String mediaId, String userId, String uuid, PostData postData, int indicatorIndex){
		Log.d (TAG, "createPostData()");
			
		waveSQLiteHelper.saveCreatePostRequest(uuid, RestClient.REST_REQUEST_STATUS_REQUESTING, 0);
		restClient.createPostData(uuid, userId, mediaId, postData.getType(), postData.getComment(), postData.getDisplayAt(), postData.getCreatedAt(), postData.getY(), indicatorIndex);

	}
	
	////
	//
	//  GET PostConts
	//
	////
	public void getPostCounts(final String mediaId){
		Log.d (TAG, "getPostCounts()");
		//TODO fork new thread
		Integer status = waveSQLiteHelper.getGetPostCountRequestStatus();
		
		if(status == null){
			
			// request does not exist, save request data and make request
			waveSQLiteHelper.saveGetPostCountRequest(RestClient.REST_REQUEST_STATUS_REQUESTING, 0);
			restClient.getPostCount(mediaId);
			
		}else if(status.intValue() == RestClient.REST_REQUEST_STATUS_COMPLETE){
			
			// no need to make request, notify via callback
			onRestProcessorListener.onGetPostCountComplete();
			
		}else if(status.intValue() == RestClient.REST_REQUEST_STATUS_REQUESTING){
			
			// request in progress. wait for 2sec and check again.
			new Handler().postDelayed(new Runnable()
			{
			    @Override
			    public void run()
			    {
			    	getPostCounts(mediaId);
			    }
			}, 2000);
			
		}else if(status.intValue() == RestClient.REST_REQUEST_STATUS_ERROR){
			// request returned with error.
			onRestProcessorListener.onError();
		}

	}
	
	private void savePostCountsToSQLite(JSONArray postCounts) {
		//Log.d (TAG, "addPosts()");
		boolean[] recordFound = {false, false, false, false, false, false, false, false, false, false, false, false, false};
		
        try {
            
            // looping through All PostCounts and add to DB
            for(int i = 0; i < postCounts.length(); i++){
                JSONObject c = postCounts.getJSONObject(i);
                int count = c.getInt("count");
                int index = c.getInt("indicator_index");
                if(index >= 0 && index < Indicator.NUM_OF_COLUMS){
                	recordFound[index] = true;
                	waveSQLiteHelper.savePostCount(index, count);
                }
            }

            for(int i=0; i<recordFound.length ;i++){
            	if(!recordFound[i]){
            		waveSQLiteHelper.savePostCount(i, 0);
            	}
            }
            
        } catch (JSONException e) {
        	Log.e(TAG, e.toString());
        	onRestProcessorListener.onError();
        }
	}	
	
    
	////
	//
	//  Callback Implementation
	//
	////		
    private RestCompleteListener mRestCompleteListener = new RestCompleteListener(){
    	
    	@Override
    	public void onGetPostDataComplete(final JSONObject jsonObject){
    		Log.d (TAG, "onGetPostDataComplete()");

    		new Thread(new Runnable() {
    	        public void run() {
    	        	if( jsonObject != null ){
			        	try {
			        		int min = ((int) (jsonObject.getInt("from") / (double) 60)) + 1;
		
			        		savePostsToSQLite(jsonObject.getJSONArray("posts"), jsonObject.getInt("from"), jsonObject.getInt("to"));
							waveSQLiteHelper.updateGetPostRequest(min, RestClient.REST_REQUEST_STATUS_COMPLETE);
							
							// notify via callback
							onRestProcessorListener.onGetPostDataComplete(min);
							
						} catch (JSONException e) {
							Log.e(TAG, e.toString());
							onRestProcessorListener.onError();
						}
    	        	}
    	        }
    		}).start();

    	}

		@Override
		public void onGetPostCountComplete(final JSONObject jsonObject) {
			Log.d (TAG, "onGetPostCountComplete()");

    		new Thread(new Runnable() {
    	        public void run() {
    	        	if( jsonObject != null ){
			        	try {
		
			        		savePostCountsToSQLite(jsonObject.getJSONArray("counts"));
							waveSQLiteHelper.updateGetPostCountRequest(RestClient.REST_REQUEST_STATUS_COMPLETE, 0);
							
							// notify via callback
							onRestProcessorListener.onGetPostCountComplete();
							
						} catch (JSONException e) {
							Log.e(TAG, e.toString());
							onRestProcessorListener.onError();
						}
    	        	}
    	        }
    		}).start();
			
		}
		
    	@Override
    	public void onCreatePostDataComplete(final JSONObject jsonObject){
    		Log.d (TAG, "onCreatePostDataComplete()");

    		new Thread(new Runnable() {
    	        public void run() {
    	        	if( jsonObject != null ){
			        	try {
			        		
			        		String uuid = jsonObject.getString("post_id") ;
							waveSQLiteHelper.updateCreatePostRequest(uuid, RestClient.REST_REQUEST_STATUS_COMPLETE, 0);
							
							// notify via callback
							onRestProcessorListener.onCreatePostDataComplete(uuid);
							
						} catch (JSONException e) {
							Log.e(TAG, e.toString());
							onRestProcessorListener.onError();
						}
    	        	}
    	        }
    		}).start();

    	}
		
    	@Override
		public void onUpdatePostCountComplete(final JSONObject jsonObject) {
    		Log.d (TAG, "onUpdatePostCountComplete()");
    		
       		new Thread(new Runnable() {
    	        public void run() {
    	        	if( jsonObject != null ){
			        	try {
			        		int count_at = jsonObject.getInt("indicator_index") ;
							waveSQLiteHelper.updateRequestingPutPostCountRequest(count_at, RestClient.REST_REQUEST_STATUS_COMPLETE, 0);
							
							// notify via callback
							onRestProcessorListener.onUpdatePostCountComplete(count_at);
							
						} catch (JSONException e) {
							Log.e(TAG, e.toString());
							onRestProcessorListener.onError();
						}
    	        	}
    	        }
    		}).start();
		}
		
    	@Override
    	public void onGetError(String url){
    		Log.e (TAG, "onGetError()");
    		//TODO handle error. show error msg??
    		onRestProcessorListener.onError();
    		
    	}
    	
    	@Override
    	public void onRESTError(){
    		//TODO handle error. show error msg??
    		onRestProcessorListener.onError();
    	}

    };
    
	////
	//
	//  Class Callbacks
	//
	////	
	public void setOnRestProcessorListener(RestProcessorListener mRestProcessorListener) {
		this.onRestProcessorListener = mRestProcessorListener;
	}
	
    public interface RestProcessorListener {
    	public void onGetPostDataComplete(int min);
    	public void onGetPostCountComplete();
    	public void onCreatePostDataComplete(String uuid);
    	public void onUpdatePostCountComplete(int count_at);
    	public void onError();
    }
    
	////
	//
	//  Class Variables
	//
	////
	private static final String TAG = "RestProcessor";
	private RestClient restClient;
	private WavePlayer wavePlayer;
	private WaveSQLiteHelper waveSQLiteHelper;
	private RestProcessorListener onRestProcessorListener;
}
