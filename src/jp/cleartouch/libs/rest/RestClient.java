package jp.cleartouch.libs.rest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class RestClient{
	
	public RestClient(){}
	
	public void getPostData(final String media_id, final int from, final int to) {
		Log.d(TAG, "getPostData("+from+","+to+")");
		new Thread(new Runnable() {
	        public void run() {
	        	JSONObject jsonObj = sendRequest(METHOD_GET, BASE_URL + "posts?media_id=" + media_id + "&from=" + from + "&to=" + to, "");
	        	if(jsonObj != null)
	        		onRestCompleteListener.onGetPostDataComplete(jsonObj);
	        }
		}).start();

	}
	
	public void getPostCount(final String media_id) {
		Log.d(TAG, "getPostCount()");
		new Thread(new Runnable() {
	        public void run() {
	        	JSONObject jsonObj = sendRequest(METHOD_GET, BASE_URL + "post_counts/" + media_id, "");
	        	if(jsonObj != null)
	        		onRestCompleteListener.onGetPostCountComplete(jsonObj);
	        }
		}).start();

	}
	
	public void createPostData(final String uuid, final String user_id, final String media_id, final int type, final String comment, final int display_at, final long created_at, final int y, final int indicatorIndex) {
		Log.d(TAG, "createPostData()");
		new Thread(new Runnable() {
	        public void run() {
	        	String json = "{\"post_id\": \"" + uuid + "\", \"media_id\": \"" + media_id + "\", \"user_id\": \"" + user_id 
	        					+ "\", \"type\": \"" + type + "\", \"comment\": \"" + comment + "\", \"display_at\": \"" + display_at 
	        					+ "\", \"created_at\": \""  + created_at + "\", \"y\": \""  + y + "\", \"indicator_index\": \"" + indicatorIndex + "\"}";
	        	JSONObject jsonObj = sendRequest(METHOD_POST, BASE_URL + "posts", json);
	        	if(jsonObj != null){
	        		onRestCompleteListener.onCreatePostDataComplete(jsonObj);
	        	}
	        }
		}).start();
	}
	
	public void updatePostCount(final String media_id, final int indicator_index) {
		Log.d(TAG, "createPostData()");
		new Thread(new Runnable() {
	        public void run() {
	        	String json = "{\"media_id\": \"" + media_id + "\", \"indicator_index\": \"" + indicator_index + "\"}";
	        	JSONObject jsonObj = sendRequest(METHOD_PUT, BASE_URL + "post_counts", json);
	        	if(jsonObj != null){
	        		onRestCompleteListener.onUpdatePostCountComplete(jsonObj);
	        	}
	        }
		}).start();
	}

	private JSONObject sendRequest(String method, String url, String json){
		HttpURLConnection urlConnection = null;
		try {
			URL urlToRequest = new URL(url);
			urlConnection = (HttpURLConnection) urlToRequest.openConnection();
			urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setRequestMethod(method);
			
			if(method == "POST" || method == "PUT"){
				urlConnection.setRequestProperty("Content-Type", "application/json");
				urlConnection.setDoOutput(true);
				urlConnection.connect();

				// send request
				byte[] outputBytes = json.getBytes("UTF-8");
				OutputStream os = urlConnection.getOutputStream();
				os.write(outputBytes);
				os.close();				
			}

			
			int statusCode = urlConnection.getResponseCode();
			
	        if ((method == "GET" || method == "PUT") && statusCode != HttpURLConnection.HTTP_OK) {
	        	// handle any other errors, like 404, 500,..
	        	Log.e (TAG, " URL:" + url + " Invalid HTTP Status Code:" + statusCode);
	        	urlConnection.disconnect();
	        	urlConnection = null;
	        	onRestCompleteListener.onGetError(url);
	        	return null;
	        }else if(method == "POST" && statusCode != HttpURLConnection.HTTP_CREATED){
	        	// handle any other errors, like 404, 500,..
	        	Log.e (TAG, " URL:" + url + " Invalid HTTP Status Code:" + statusCode);
	        	urlConnection.disconnect();
	        	urlConnection = null;
	        	onRestCompleteListener.onRESTError();
	        	return null;
	        }
	        
			// get Response
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			return new JSONObject(getResponseText(in, url));
			
		} catch (MalformedURLException e) {
	        // URL is invalid
			Log.e (TAG, e.toString());
	    	onRestCompleteListener.onRESTError();
		} catch (SocketTimeoutException e) {
	        // data retrieval or connection timed out
			Log.e (TAG, e.toString());
	    	onRestCompleteListener.onRESTError();
		} catch (IOException e) {
			Log.e (TAG, e.toString());
		} catch (JSONException e) {
	        // response body is no valid JSON string
			Log.e (TAG, e.toString());
	    	onRestCompleteListener.onRESTError();
	    } finally {
	        if (urlConnection != null) {
	            urlConnection.disconnect();
	            urlConnection = null;
	        }
	    }
		
		return null;		
	}
/*	
	private JSONObject doPost(String url, String json){
		HttpURLConnection urlConnection = null;
		try {
			URL urlToRequest = new URL(url);
			urlConnection = (HttpURLConnection) urlToRequest.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setRequestMethod("POST");
			urlConnection.connect();
			
			// send request
			byte[] outputBytes = json.getBytes("UTF-8");
			OutputStream os = urlConnection.getOutputStream();
			os.write(outputBytes);
			os.close();
			
			int statusCode = urlConnection.getResponseCode();
	        if (statusCode != HttpURLConnection.HTTP_CREATED) {
	            // handle any other errors, like 404, 500,..
	        	Log.e (TAG, "Invalid HTTP Status Code:" + statusCode);
	        	urlConnection.disconnect();
	        	urlConnection = null;
	        	onRestCompleteListener.onRESTError();
	        	return null;
	        }
	        
			// get Response
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			return new JSONObject(getResponseText(in, url));
			
		} catch (MalformedURLException e) {
	        // URL is invalid
			Log.e (TAG, e.toString());
	    	onRestCompleteListener.onRESTError();
		} catch (SocketTimeoutException e) {
	        // data retrieval or connection timed out
			Log.e (TAG, e.toString());
	    	onRestCompleteListener.onRESTError();
		} catch (IOException e) {
			Log.e (TAG, e.toString());
		} catch (JSONException e) {
	        // response body is no valid JSON string
			Log.e (TAG, e.toString());
	    	onRestCompleteListener.onRESTError();
	    } finally {
	        if (urlConnection != null) {
	            urlConnection.disconnect();
	            urlConnection = null;
	        }
	    }
		
		return null;
	}
	
	private JSONObject doGet(String url){
		HttpURLConnection urlConnection = null;
	    try {
	        // create connection
	        URL urlToRequest = new URL(url);
	        urlConnection = (HttpURLConnection) urlToRequest.openConnection();
	        urlConnection.setRequestProperty("Accept", "application/json");
	        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
	        urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

	        // handle issues
	        int statusCode = urlConnection.getResponseCode();
	        if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
	            // handle unauthorized (if service requires user login)
	        } else if (statusCode != HttpURLConnection.HTTP_OK) {
	            // handle any other errors, like 404, 500,..
	        	Log.e (TAG, "Invalid HTTP Status Code:" + statusCode);
	        	urlConnection.disconnect();
	        	urlConnection = null;
	        	onRestCompleteListener.onGetError(url);
	        	return null;
	        }
	        
	        // create JSON object from content
	        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
	        return new JSONObject(getResponseText(in, url));
	    } catch (MalformedURLException e) {
	        // URL is invalid
	    	Log.e (TAG, e.toString());
	    	onRestCompleteListener.onGetError(url);
	    } catch (SocketTimeoutException e) {
	        // data retrieval or connection timed out
	    	//TODO hide loading? retry? show error msg?
	    	Log.e (TAG, e.toString());
	    	onRestCompleteListener.onGetError(url);
	    } catch (IOException e) {
	        // could not read response body 
	        // (could not create input stream)
	    	Log.e (TAG, e.toString());
	    	onRestCompleteListener.onGetError(url);
	    } catch (JSONException e) {
	        // response body is no valid JSON string
	    	Log.e (TAG, e.toString());
	    	onRestCompleteListener.onGetError(url);
	    } finally {
	        if (urlConnection != null) {
	            urlConnection.disconnect();
	            urlConnection = null;
	        }
	    }
	    
	    return null;
	}
*/
	
	private String getResponseText(InputStream inStream, String url) {
	    // very nice trick from 
	    // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
		String str = null;
		try{
			str = new Scanner(inStream).useDelimiter("\\A").next();
		} catch (Exception e){
			onRestCompleteListener.onRESTError();
		} 
	    return str;
	}
	
	
	////
	//
	//  Class Callbacks
	//
	////	
	public void setOnRestCompleteListener(RestCompleteListener mRestCompleteListener) {
		this.onRestCompleteListener = mRestCompleteListener;
	}
	
    public interface RestCompleteListener {
    	public void onGetPostDataComplete(JSONObject jsonObject);
    	public void onGetPostCountComplete(JSONObject jsonObject);
    	public void onCreatePostDataComplete(JSONObject jsonObject);
    	public void onUpdatePostCountComplete(JSONObject jsonObject);
    	public void onGetError(String url);
    	public void onRESTError();
    }
	
	////
	//
	//  Class Variables
	//
	////    
    private static final String TAG = RestClient.class.getSimpleName();
	
    public static final int REST_REQUEST_STATUS_REQUESTING = 0;
	public static final int REST_REQUEST_STATUS_COMPLETE = 1;
	public static final int REST_REQUEST_STATUS_ERROR = 2;
	private static final int CONNECTION_TIMEOUT = 10000;
	private static final int DATARETRIEVAL_TIMEOUT = 10000;
	private static final String METHOD_GET = "GET";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_PUT = "PUT";
	
	private static final String BASE_URL = "http://waverestserver-env-u7228bsevb.elasticbeanstalk.com/0.1/";
	
	private RestCompleteListener onRestCompleteListener;
}
