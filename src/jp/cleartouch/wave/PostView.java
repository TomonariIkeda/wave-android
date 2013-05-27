package jp.cleartouch.wave;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jp.cleartouch.postcast.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PostView extends RelativeLayout{
    @SuppressLint("NewApi")
	public PostView(WavePlayer wavePlayer) {
        super(wavePlayer.getActivity());

   this.setBackgroundColor(Color.RED);
        this.setVisibility(View.INVISIBLE);
        
        this.context = wavePlayer.getActivity();
        this.screenWidth = wavePlayer.getScreenWidth();
        
        // contentText
        commentText = new TextView(context);
        commentText.setId(Helpers.generateViewId());
        commentText.setTextColor(Color.BLACK);
        commentText.setTypeface(null, Typeface.BOLD);
        commentText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        commentText.setBackgroundResource(R.drawable.postview_bg);
        commentText.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams contentTextLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        contentTextLayoutParams.topMargin = (int) Helpers.convertDpToPixel(16, context);
        contentTextLayoutParams.rightMargin = (int) Helpers.convertDpToPixel(5, context);
        this.addView(commentText, contentTextLayoutParams);
        
        // sticker
        sticker = new ImageView(context);
        sticker.setId(Helpers.generateViewId());
        sticker.setImageDrawable(context.getResources().getDrawable(R.drawable.clap_screen));
        sticker.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams stickerLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        stickerLayoutParams.topMargin = (int) Helpers.convertDpToPixel(16, context);
        stickerLayoutParams.rightMargin = (int) Helpers.convertDpToPixel(5, context);
        this.addView(sticker, stickerLayoutParams);
        
        // userName
        userName = new TextView(context);
        userName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        userName.setTextColor(Color.WHITE);
        userName.setShadowLayer(1.5f, 1, 1, Color.BLACK);
        RelativeLayout.LayoutParams userNameLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        userNameLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, commentText.getId());
        this.addView(userName, userNameLayoutParams);
        
        // thumb
        thumbnail = new ImageView(context);
        thumbnail.setId(Helpers.generateViewId());
        RelativeLayout.LayoutParams thumbnailLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        thumbnailLayoutParams.addRule(RelativeLayout.RIGHT_OF, commentText.getId());
        this.addView(thumbnail, thumbnailLayoutParams);
        
        // createdTime
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
    
    public void setData(int type, String comment, byte[] thumb_data, String user_name,
			long created_at, int y, int display_at, String color) {
    	
    	// size of thumb is 37dp x 37dp
    	int thumbSize = (int) Helpers.convertDpToPixel(37, this.context);
    	Bitmap thumb = BitmapFactory.decodeByteArray(thumb_data, 0, thumb_data.length);
    	Bitmap resizedBitmap = Bitmap.createScaledBitmap(thumb, thumbSize, thumbSize, false);     	
    	thumbnail.setImageBitmap(resizedBitmap);

    	displayAt = display_at;
    	this.setYCoord(y);

    	userName.setText(user_name);
        userName.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int userNameWidth = userName.getMeasuredWidth();
    	commentText.setMinimumWidth( userNameWidth );
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("hh:mma", Locale.US);
    	//TODO how to switch between time zones?    	
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT+09:00"));
    	String createdAtString = sdf.format(new Date(created_at*1000));
    	createdTime.setText(createdAtString); 	
    	    	
    	if(type == 0){
    		// clap
    		sticker.setVisibility(View.VISIBLE);
    		commentText.setVisibility(View.INVISIBLE);
    	}else if(type == 1){
    		// comment
    		commentText.setText(comment);
    		sticker.setVisibility(View.INVISIBLE);
    		commentText.setVisibility(View.VISIBLE);
    		if(color=="white"){
        		commentText.setBackgroundResource(R.drawable.postview_bg);
        	}else if(color=="yellow"){
        		commentText.setBackgroundResource(R.drawable.postview_yellow_bg);
        	}
    	}
        
        this.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    	this.slideInAnimationVelocity = (int) ((this.getMeasuredWidth() + screenWidth) * 1000 / (double) WavePlayer.SLIDE_IN_DURATION);
    	
	}
    
    public void setCommentText(String text){
    	this.commentText.setText(text);
    }
    
    public void setUserName(String user_name){
    	this.userName.setText(user_name);
    }
    
    public void setThumbnail(int thumb_res_id){
    	this.thumbnail.setImageResource(thumb_res_id);
    }
 
    public void setDuration(int duration){
        this.slideInAnimation.setDuration(duration);
    }
    
    public void setYCoord(int y){
    	layoutParams.topMargin = (int) Helpers.convertDpToPixel(y, this.getContext());
    	this.yCoord = y;
    }
    
    public void setDisplayAt(int displayAt){
    	this.displayAt = displayAt;
    }
 
    public TextView getContentText(){
    	return this.commentText;
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

    
    // view properties
    private Context context;
    private TextView commentText;
    private ImageView thumbnail;
    private ImageView sticker;
    private TextView userName;
    private TextView createdTime;
    private int screenWidth;
    private int slideInAnimationVelocity;
    
    private RelativeLayout.LayoutParams layoutParams;
    private SlideInAnimation slideInAnimation;
    private NewPostAnimation newPostAnimation;
}
