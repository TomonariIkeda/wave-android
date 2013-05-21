package jp.cleartouch.wave;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

public class NewPostAnimation extends TranslateAnimation{

    public NewPostAnimation(PostView postView) {
    	super(
                Animation.RELATIVE_TO_SELF,   //fromXType 
                0.0f,                         //fromXValue
                Animation.RELATIVE_TO_PARENT, //toXType
                -1.0f,                        //toXValue
                Animation.RELATIVE_TO_SELF,   //fromYType 
                0.0f,                         //fromYValue
                Animation.RELATIVE_TO_SELF,   //toYType
                0.0f                          //toYValue
              );
    	// set reference to PostView
    	pv = postView;
    }

    private final String TAG = "NewPostAnimation";
    private long mElapsedAtPause=0;
    private boolean mPaused=false;
    private boolean pauseAfterStart=false;
    private PostView pv;
    
    
    @Override
    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if(mPaused && mElapsedAtPause==0) {
            mElapsedAtPause=currentTime-getStartTime();
        }
        if(mPaused)
            setStartTime(currentTime-mElapsedAtPause);

        return super.getTransformation(currentTime, outTransformation);
    }

    public void pause() {
    	Log.e(TAG, " pause()");
        mElapsedAtPause=0;
        mPaused=true;
    }
    
    public void resume() {
    	Log.e(TAG, "");
        mPaused=false;
    }
    
    public void onAnimationStart(){
    	if( pauseAfterStart ){
    		pauseAfterStart=false;
    		pause();
    	}
    }
    
    public PostView getPostView(){
    	return pv;
    }
    
    public void setPauseAfterStart(boolean value) {
    	pauseAfterStart=value;
    }

    public boolean getPauseAfterStart() {
    	return pauseAfterStart;
    }
    
    
}