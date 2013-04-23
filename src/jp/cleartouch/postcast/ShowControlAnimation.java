package jp.cleartouch.postcast;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class ShowControlAnimation extends TranslateAnimation{

    public ShowControlAnimation() {
    	super(
                Animation.RELATIVE_TO_SELF,   //fromXType 
                0.0f,                         //fromXValue
                Animation.RELATIVE_TO_PARENT, //toXType
                0.0f,                        //toXValue
                Animation.RELATIVE_TO_SELF,   //fromYType 
                -1.0f,                         //fromYValue
                Animation.RELATIVE_TO_SELF,   //toYType
                0.0f                          //toYValue
              );
    }

}