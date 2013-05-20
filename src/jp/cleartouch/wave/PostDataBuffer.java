package jp.cleartouch.wave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import jp.cleartouch.sqlite.WaveSQLiteHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

public class PostDataBuffer extends SparseArray<ArrayList<PostData>>{
	
	private WavePlayer wavePlayer;
	
	public PostDataBuffer(WavePlayer wp){
		super();
		this.wavePlayer = wp;
	}

	public void startPushingData(int currentSec) {
		Log.d (TAG, "startPushingData("+currentSec+")");
		
		isPushingStarted = true;
		dbHelperObject = new WaveSQLiteHelper(wavePlayer.getActivity());
		SQLiteDatabase db = dbHelperObject.getReadableDatabase();
	    
		// for first 20 sec. starting from currentSec - 4
		int fromSec = (currentSec - 4 >= 0) ? currentSec - 4 : 0;
        for(int i = fromSec; i < fromSec + INITIAL_BUFFER_SIZE; i++){
        	
        	this.put(i, new ArrayList<PostData>());
        	this.largestKey = i;
        	
        	readDataFromSQLite(db, i);
        	
        } // end of for loop
        db.close();
        
        onPostDataBufferListener.onPostDataBufferReady(fromSec);
	}
	
	/*
	 * called when seeking.
	 */
	public void stopPushingData(){
		isPushingStarted = false;
		this.clear();
	}
	
	private void readDataFromSQLite(SQLiteDatabase db, int displayAt){
		
		// retrieve data from SQLite
    	Cursor cursor = db.query(WaveSQLiteHelper.TABLE_POST, 
    			new String[] { WaveSQLiteHelper.TABLE_POST_COLUMN_DATA, WaveSQLiteHelper.TABLE_POST_COLUMN_Y,
    						WaveSQLiteHelper.TABLE_POST_COLUMN_THUMB, WaveSQLiteHelper.TABLE_POST_COLUMN_USERNAME},
				WaveSQLiteHelper.TABLE_POST_COLUMN_DISPLAY_AT + "=?",
	            new String[] { String.valueOf(displayAt) }, null, null, null, null);
	   
    	if (cursor != null)
	        cursor.moveToFirst();
    	
    	while( ! cursor.isAfterLast() ){
    		String dataString = cursor.getString(0);
    	    int yCoord = Integer.parseInt(cursor.getString(1));
    	    String thumb = cursor.getString(2);
            byte[] thumbData = Base64.decode(thumb, Base64.DEFAULT);
        	String userName = cursor.getString(3);
        	addPostData(new PostData(wavePlayer.getActivity(), yCoord, wavePlayer.getScreenWidth(), displayAt, dataString, thumbData, userName, "Apr12 '13", "10:25AM"));
        	
        	// move cursor
        	cursor.moveToNext();
    	}
    	
    	cursor.close();
	}
	
	public void addPostData(PostData postData) {
		ArrayList<PostData> list = this.get(postData.getDisplayAt());
		list.add(postData);
	}
	
	private void pushNextData() {
		
		dbHelperObject = new WaveSQLiteHelper(wavePlayer.getActivity());
		SQLiteDatabase db = dbHelperObject.getReadableDatabase();
		
		largestKey++;
		this.put(largestKey, new ArrayList<PostData>());
    	readDataFromSQLite(db, largestKey);
    	
    	db.close();
	}
	
	/*
	 * this function gets called every cycle.
	 */
	public ArrayList<PostData> getPdListAt(int sec){
		
		ArrayList<PostData> list = this.get(sec);
		this.delete(sec);
		
		if( largestKey < wavePlayer.getDurationInSec() ){
			pushNextData();
		}

		return list;
	}

	public boolean isPushingStarted(){
		return isPushingStarted;
	}
	
	public void setOnPostDataBufferListener(PostDataBufferListener mPostDataBufferListener) {
		this.onPostDataBufferListener = mPostDataBufferListener;
	}
	
    public interface PostDataBufferListener {
    	public void onPostDataBufferReady(int sec);
    }
    
	private static final String TAG = PostDataBuffer.class.getSimpleName();
	public static final int INITIAL_BUFFER_SIZE = 20; // Try to retrieve data if the size of buffer gets less than this.
	
	private boolean isPushingStarted = false; // true if pushing data has started.
	
	private int largestKey = 0; // largest key of PostDataBuffer. key represents elapsed time.
	private PostDataBufferListener onPostDataBufferListener;
	private WaveSQLiteHelper dbHelperObject;
}
