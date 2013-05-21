package jp.cleartouch.wave;



import jp.cleartouch.postcast.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PostView extends RelativeLayout{
    @SuppressLint("NewApi")
	public PostView(Context context) {
        super(context);

        //this.setBackgroundColor(Color.RED);
        this.setVisibility(View.INVISIBLE);
        
        this.context = context;
        
        // contentText
        contentText = new TextView(context);
        contentText.setId(Helpers.generateViewId());
        contentText.setTextColor(Color.BLACK);
        contentText.setTypeface(null, Typeface.BOLD);
        contentText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        contentText.setBackgroundResource(R.drawable.postview_bg);
        // set min length and max lines so that username will be displayed correctly
        contentText.setMinimumWidth((int) Helpers.convertDpToPixel(85, context));
        RelativeLayout.LayoutParams contentTextLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        contentTextLayoutParams.topMargin = (int) Helpers.convertDpToPixel(16, context);
        contentTextLayoutParams.rightMargin = (int) Helpers.convertDpToPixel(5, context);
        this.addView(contentText, contentTextLayoutParams);
        
        // userName
        userName = new TextView(context);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        userName.setTextColor(Color.WHITE);
        userName.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        RelativeLayout.LayoutParams userNameLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        userNameLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, contentText.getId());
        this.addView(userName, userNameLayoutParams);
        
        // thumb
        thumbnail = new ImageView(context);
        thumbnail.setId(Helpers.generateViewId());
        RelativeLayout.LayoutParams thumbnailLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        thumbnailLayoutParams.addRule(RelativeLayout.RIGHT_OF, contentText.getId());
        this.addView(thumbnail, thumbnailLayoutParams);

        // postDate
        /*
        createdDate = new TextView(context);
        createdDate.setId(Helpers.generateViewId());
        createdDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);
        createdDate.setTextColor(Color.WHITE);
        createdDate.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        RelativeLayout.LayoutParams createdDateLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        createdDateLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, thumbnail.getId());
        createdDateLayoutParams.addRule(RelativeLayout.BELOW, thumbnail.getId());
        this.addView(createdDate, createdDateLayoutParams);
        */
        
        // postTime
        createdTime = new TextView(context);
        createdTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
        createdTime.setTextColor(Color.WHITE);
        createdTime.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        RelativeLayout.LayoutParams createdTimeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        createdTimeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, thumbnail.getId());
        createdTimeLayoutParams.addRule(RelativeLayout.BELOW, thumbnail.getId());
        //createdTimeLayoutParams.topMargin = (int) Helpers.convertDpToPixel(-3, context);
        this.addView(createdTime, createdTimeLayoutParams);
        
        // layout
        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        setLayoutParams(layoutParams);
        
        // animation
		slideInAnimation = new SlideInAnimation(this);
        slideInAnimation.setInterpolator(new LinearInterpolator());
        slideInAnimation.setDuration(WavePlayer.SLIDE_IN_DURATION);

        // animation
		newPostAnimation = new NewPostAnimation(this);
        newPostAnimation.setInterpolator(new LinearInterpolator());
        newPostAnimation.setDuration(WavePlayer.NEW_POST_DURATION);
    }
    

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
    	
    	if( xNew > 0 && yNew > 0 ){
    		//Log.d (TAG, "Dim of PostView(" + this.contentText.getText() + "): " + (int) Helpers.convertPixelsToDp(this.getHeight(), this.getContext())  + "," + (int) Helpers.convertPixelsToDp(this.getWidth(), this.getContext()));
    		//this.state = STATE_PREPARED;
    	}
    	
    	super.onSizeChanged(xNew, yNew, xOld, yOld);
    }
    
    public void startSlideIn(){
    	//Log.d (TAG, "startSlideIn()");
    	this.setVisibility(View.VISIBLE);
    	this.startAnimation(this.slideInAnimation);
    }
    
    public void startNewPostAnimation(){
    	this.setVisibility(View.VISIBLE);
    	this.startAnimation(this.newPostAnimation);    	
    }
    
    public void pauseAfterStartNewPostAnimation(){
    	this.newPostAnimation.setPauseAfterStart(true);
    	this.setVisibility(View.VISIBLE);
    	this.startAnimation(this.newPostAnimation);  
    }
    
    public void pauseAnimation(){
    	this.slideInAnimation.pause();
    	this.newPostAnimation.pause();
    }

    /*
     * call before animationMove()
     */
    public void animationSetTimeToMove(long milisec){
    	this.slideInAnimation.setTimeToMove(milisec);
    }
    
    /*
     * timeToMove has to be set beforehand by calling animationSetTimeToMove()
     */
    public void animationMove(){
    	this.slideInAnimation.move();
    }
    
    public void resumeAnimation(){
    	this.slideInAnimation.resume();
    	this.newPostAnimation.resume();
    }
    
    public void cancelAnimation(){
    	this.slideInAnimation.cancel();
    	this.newPostAnimation.cancel();
    }
    
    public void setData(String text, byte[] thumb_data, String user_name,
			String created_date, String created_time, int y, int display_at, String color, int velocity) {

    	contentText.setText(text);
    	
    	// size of thumb is 37dp x 37dp
    	int thumbSize = (int) Helpers.convertDpToPixel(37, this.context);
    	Bitmap thumb = BitmapFactory.decodeByteArray(thumb_data, 0, thumb_data.length);
    	Bitmap resizedBitmap = Bitmap.createScaledBitmap(thumb, thumbSize, thumbSize, false);     	
    	thumbnail.setImageBitmap(resizedBitmap);
    	userName.setText(user_name);
    	createdTime.setText(created_time);
    	displayAt = display_at;
    	this.setYCoord(y);
    	this.slideInAnimationVelocity = velocity;
    	
    	if(color=="white"){
    		contentText.setBackgroundResource(R.drawable.postview_bg);
    	}else if(color=="yellow"){
    		contentText.setBackgroundResource(R.drawable.postview_yellow_bg);
    	}
	}
    
    public void setContentText(String text){
    	this.contentText.setText(text);
    }
    
    public void setUserName(String user_name){
    	this.userName.setText(user_name);
    }
    
    public void setThumbnail(int thumb_res_id){
    	this.thumbnail.setImageResource(thumb_res_id);
    }
    
    /*
    public void setCreatedDate(String date){
    	this.createdDate.setText(date);
    }
    */
    
    public void setCreatedTime(String time){
    	this.createdTime.setText(time);
    }
 
    public void setDuration(int duration){
        this.slideInAnimation.setDuration(duration);
    }
    
    private void setYCoord(int y){
    	layoutParams.topMargin = (int) Helpers.convertDpToPixel(y, this.getContext());
    	this.yCoord = y;
    }
    
    public void setDisplayAt(int displayAt){
    	this.displayAt = displayAt;
    }
 
    public TextView getContentText(){
    	return this.contentText;
    }
    
    public int getDisplayAt(){
    	return displayAt;
    }
    
    public int getYCoord(){
    	return yCoord;
    }
    
    public int getSlideInAnimationVelocity(){
    	return slideInAnimationVelocity;
    }
    
    public SlideInAnimation getSlideInAnimation(){
    	return slideInAnimation;
    }
    
    public NewPostAnimation getNewPostAnimation(){
    	return newPostAnimation;
    }

    
	////
	//
	//  Class Variables
	//
	////
    private static final String TAG = "PostView";
    
    private int displayAt; // duration time in sec
    private int yCoord; // yCoord of postview in dp
    private int state; // PREPARING, PREPARED, STARTED, FINISHED
    private static final int STATE_PREPARING = 0;
    private static final int STATE_PREPARED = 1;
    private static final int STATE_STARTED = 2;
    private static final int STATE_FINISHED = 3;
    
    
    // view properties
    private Context context;
    private TextView contentText;
    private ImageView thumbnail;
    private TextView userName;
    //private TextView createdDate;
    private TextView createdTime;
    private int slideInAnimationVelocity;
    
    private RelativeLayout.LayoutParams layoutParams;
    private SlideInAnimation slideInAnimation;
    private NewPostAnimation newPostAnimation;
}
