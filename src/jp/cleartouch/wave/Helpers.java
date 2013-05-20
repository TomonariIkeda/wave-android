package jp.cleartouch.wave;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Helpers {
	
	/**
	 * This method convets dp unit to equivalent device specific value in pixels. 
	 * 
	 * @param dp A value in dp(Device independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent Pixels equivalent to dp according to device
	 */
	public static float convertDpToPixel(float dp,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return px;
	}
	
	/**
	 * This method converts device specific pixels to device independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent db equivalent to px value
	 */
	public static float convertPixelsToDp(float px,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;

	}
    
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

	public static CharSequence formatMilliseconds(int millisec) {
		
		String formatted = "";
		
		int seconds = (int) (millisec / 1000) % 60 ;
		int minutes = (int) ((millisec / (1000*60)) % 60);
		int hours   = (int) ((millisec / (1000*60*60)) % 24);
		
		if(hours > 0){
			formatted += hours + ":";
			formatted += (minutes > 9 ) ?  minutes + ":" : "0" + minutes + ":";
			formatted += (seconds > 9 ) ?  seconds : "0" + seconds;
		}else{
			// less than hour
			formatted += minutes + ":";
			formatted += (seconds > 9 ) ?  seconds : "0" + seconds;
		}
		
		return formatted;
	}
    
}
