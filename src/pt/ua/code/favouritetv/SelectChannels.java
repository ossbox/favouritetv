package pt.ua.code.favouritetv;

import java.util.LinkedList;
import java.util.List;

import pt.ua.code.favouritetv.content.SQLiteFavouriteTvHelper;
import pt.ua.code.favouritetv.gui.ImagemETextoListAdapter;
import pt.ua.code.ws.Channel;
import pt.ua.code.ws.MeoWsClient;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class SelectChannels extends Activity {
	private ListView listview;
	private List<String> listachannels;
	private SQLiteFavouriteTvHelper db;
	private EditText search;
	private ImagemETextoListAdapter adapter;
	private TextWatcher filterTextWatcher = new TextWatcher() {

	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	    	if(adapter!=null)
	    		adapter.getFilter().filter(s);
	    }

		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.favouritechannels);
		db = new  SQLiteFavouriteTvHelper(this);
		listview =(ListView) this.findViewById(R.id.lstMeochannels);
		search =(EditText) this.findViewById(R.id.editsearch);
		search.addTextChangedListener(filterTextWatcher);
		
		
		List<Channel> channels = FavouriteTV.getFavouriteTvChannels();
		listachannels=new LinkedList<String>();
		if(channels == null || channels.size()==0){
			MeoWsClient wsc = new MeoWsClient(this);
	        channels = wsc.getChannelsRest();
	        
	        for(Channel c : channels ){
	        	db.insertChannel(c);
	        	listachannels.add(c.getSigla());
	        }
		}
        if(channels!=null && channels.size()>0){
        	
        	if(listachannels.size()==0){
		        for(Channel c : channels){
		        	listachannels.add(c.getSigla());
		        }
        	}
        	
        	adapter = new ImagemETextoListAdapter(this, listachannels, listview,R.layout.imagetext);
	        listview.setAdapter(adapter);
	        listview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					RatingBar r = (RatingBar)arg1.findViewById(R.id.ratingfavourite);
					Integer rate = ((ImagemETextoListAdapter)listview.getAdapter()).getRatingValue((String)r.getTag());
					if(rate==0){
						r.setRating(1);
						db.setFavourite(((TextView)arg1.findViewById(R.id.idTextView)).getText().toString(), 1);
						((ImagemETextoListAdapter)listview.getAdapter()).setRatingValue((String)r.getTag(), 1);
					}else{
						r.setRating(0);
						db.setFavourite(((TextView)arg1.findViewById(R.id.idTextView)).getText().toString(), 0);
						((ImagemETextoListAdapter)listview.getAdapter()).setRatingValue((String)r.getTag(), 0);
					}
				}
			});
	        
        }
	}
	
	
}
