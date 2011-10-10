package pt.ua.code.favouritetv.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class FViewFlipper extends ViewFlipper {

	public FViewFlipper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onDetachedFromWindow() {
		try {
	        super.onDetachedFromWindow();
	    }
	    catch (IllegalArgumentException e) {
	        stopFlipping();
	    }
	}

}
