package jp.cleartouch.libs.rest;

import jp.cleartouch.libs.rest.RestClient.RestCompleteListener;
import jp.cleartouch.sqlite.WaveSQLiteHelper;
import jp.cleartouch.wave.PostData;
import jp.cleartouch.wave.WavePlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

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
                String userName = c.getString("userName");
                String thumb = c.getString("thumb");
                String dataString = c.getString("data");
                int y = c.getInt("y");
                int displayAt = c.getInt("postedAt");
                
                waveSQLiteHelper.savePost(y, displayAt, dataString, thumb, userName, "Apr12 '13", "10:25AM");
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
	public void createPostData(final String mediaId, String userId, String uuid, PostData postData){
		Log.d (TAG, "createPostData()");
			
		waveSQLiteHelper.saveCreatePostRequest(uuid, RestClient.REST_REQUEST_STATUS_REQUESTING, 0);
		restClient.createPostData(uuid, userId, mediaId, postData.getText(), postData.getDisplayAt(), postData.getY());

	}
	
	
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
    	public void onGetError(String url){
    		Log.e (TAG, "onGetError()");
    		//TODO handle error. show error msg??
    		onRestProcessorListener.onError();
    		
    		/*
    		Uri uri = Uri.parse(url);
    		if( uri.getLastPathSegment() == "posts" ){
    			// error in getPosts request.
    			//BASE_URL + "posts?media_id=" + media_id + "&from=" + from + "&to=" + to
    			String from = uri.getQueryParameter("from");
    			int min = ((int) (Integer.parseInt(from) / (double) 60)) + 1;
    			updatePostGetRequest(min, WaveSQLiteHelper.TABLE_GET_POST_REQUEST_STATUS_ERROR);
    		}
    		*/
    		
    	}
    	
    	@Override
    	public void onRESTError(){
    		//TODO handle error. show error msg??
    		onRestProcessorListener.onError();
    	}

    };
    
	////
	//
	//  Callbacks
	//
	////	
	public void setOnRestProcessorListener(RestProcessorListener mRestProcessorListener) {
		this.onRestProcessorListener = mRestProcessorListener;
	}
	
    public interface RestProcessorListener {
    	public void onGetPostDataComplete(int min);
    	public void onCreatePostDataComplete(String uuid);
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
