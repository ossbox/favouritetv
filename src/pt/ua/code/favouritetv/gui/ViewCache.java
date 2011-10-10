package pt.ua.code.favouritetv.gui;

import pt.ua.code.favouritetv.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class ViewCache {

	private View baseView;
	private TextView textView;
	private ImageView imageView;
	private RatingBar rating;
	
	public ViewCache(View baseView) {
		this.baseView = baseView;
	}

	public TextView getTextView() {
		if (textView == null) {
			textView = (TextView) baseView.findViewById(R.id.idTextView);
		}
		return textView;
	}
	
	public ImageView getImageView() {
		if (imageView == null) {
			imageView = (ImageView) baseView.findViewById(R.id.idImageView);
		}
		return imageView;
	}
	public RatingBar getRating() {
		if (rating == null) {
			rating = (RatingBar) baseView.findViewById(R.id.ratingfavourite);
		}
		return rating;
	}
	public View getBaseView() {
		return baseView;
	}
}