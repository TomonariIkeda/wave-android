package jp.cleartouch.wave;

import java.io.IOException;
import org.json.JSONObject;
import jp.cleartouch.libs.rest.RestClient;
import jp.cleartouch.libs.rest.RestClient.RestCompleteListener;
import jp.cleartouch.wave.R;
import jp.cleartouch.sqlite.WaveSQLiteHelper;
import android.os.Bundle;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

public class MainActivity extends Activity {

	private static final String TAG = "PlayerActivity";
	
	private WavePlayer wavePlayer;
	private RestClient restClient;
	private EditText postEditText;
	private String mediaId;
	private String userId;
	private String userThumbString;
	private byte[] userThumbData;
	private String userName;
	private WaveSQLiteHelper dbHelperObject;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.getWindow()
			.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN | LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		
		String audioUrl = "http://dev.cleartouch.jp/akb_radio.mp3";
		String dataUrl = "http://dev.cleartouch.jp/postdata.json";
		
		mediaId = "f2685680-ab25-11e2-9386-bb93a2fa9342";
		userId = "7a67e790-acc9-11e2-84c8-211d138a9724";
		userThumbString = "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";
		userThumbData = Base64.decode(userThumbString, Base64.DEFAULT);
		userName = "tomo";
		restClient = new RestClient();
		
		// create new table for PostData, GetPostRequest, PostPostRequest
		dbHelperObject = new WaveSQLiteHelper(this);
		SQLiteDatabase db = dbHelperObject.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + WaveSQLiteHelper.TABLE_POST);
		db.execSQL("DROP TABLE IF EXISTS " + WaveSQLiteHelper.TABLE_POST_COUNT);
		db.execSQL("DROP TABLE IF EXISTS " + WaveSQLiteHelper.TABLE_GET_POST_REQUEST);
		db.execSQL("DROP TABLE IF EXISTS " + WaveSQLiteHelper.TABLE_GET_POST_COUNT_REQUEST);
		db.execSQL("DROP TABLE IF EXISTS " + WaveSQLiteHelper.TABLE_CREATE_POST_REQUEST);
		dbHelperObject.createTablePost(db);
		dbHelperObject.createTablePostCount(db);
		dbHelperObject.createTableGetPostRequest(db);
		dbHelperObject.createTableGetPostCountRequest(db);
		dbHelperObject.createTableCreatePostRequest(db);
		db.close();

		try{
			wavePlayer = new WavePlayer(this, mediaId, 458, userName, userThumbData);
			wavePlayer.setDataSource(audioUrl, dataUrl);		
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + audioUrl, ex);
			return;
		}
		catch(IllegalArgumentException ex){
			Log.w(TAG, "Unable to open content: " + audioUrl, ex);
			return;
		}
		
		// register listeners
		this.restClient.setOnRestCompleteListener(mRestCompleteListener);

		/*
		postEditText.setOnTouchListener(new View.OnTouchListener(){
		    public boolean onTouch(View view, MotionEvent event) {                                                       
		         Log.d (TAG, "EditText onTouch()");
		         if(event.getAction() == MotionEvent.ACTION_DOWN){
		         }
		         //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);                
		         return false;
		    }
		});
		*/
		
		
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
		wavePlayer.pausePlayer();
			
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
	    
	    wavePlayer.release(); 
		wavePlayer = null;
		
	    Log.d(TAG, "onDestroy");
	}

private RestCompleteListener mRestCompleteListener = new RestCompleteListener(){

		@Override
		public void onCreatePostDataComplete(JSONObject jsonObject) { }

		@Override
		public void onRESTError() { }

		@Override
		public void onGetError(String url) { }

		@Override
		public void onGetPostDataComplete(JSONObject jsonObject) { }

		@Override
		public void onGetPostCountComplete(JSONObject jsonObject) { }

		@Override
		public void onUpdatePostCountComplete(JSONObject jsonObject) { }
		
    };
    
}
