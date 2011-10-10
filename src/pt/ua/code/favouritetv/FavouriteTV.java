package pt.ua.code.favouritetv;

import java.util.LinkedList;
import java.util.List;

import pt.ua.code.favouritetv.content.SQLiteFavouriteTvHelper;
import pt.ua.code.favouritetv.gui.ImagemETextoListAdapter;
import pt.ua.code.favouritetv.service.ContextAlertBinder;
import pt.ua.code.favouritetv.service.ContextAlerter;
import pt.ua.code.ws.Channel;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class FavouriteTV extends Activity implements OnClickListener {

	private SQLiteFavouriteTvHelper db;
	private static LinkedList<Channel> favouriteTvChannels;
	private List<String> listachannels;
	private ListView listview;
	private ServiceConnection mContextAlerterConnection = new ContextAlerterServiceConnection();
	private static ContextAlerter mContextAlerterService;
	private Button btnHome, btnPrograms;
	public final int ID_FAVOURITE=0x11;
	
	public static ContextAlerter getContextAlerterService(){
		return mContextAlerterService;
	}
	
	public static LinkedList<Channel> getFavouriteTvChannels() {
		return favouriteTvChannels;
	}
	
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent serviceIntent = new Intent("pt.ua.code.favouritetv.service.ContextAlerter");
		startService(serviceIntent);
		bindService(serviceIntent, mContextAlerterConnection, Context.BIND_AUTO_CREATE);
		btnHome = (Button)this.findViewById(R.id.main_casa);
		btnPrograms = (Button)this.findViewById(R.id.main_program);
		btnHome.setOnClickListener(this);
		btnPrograms.setOnClickListener(this);
		listview =(ListView) this.findViewById(R.id.listFavourites);
        db=new SQLiteFavouriteTvHelper(this);
        favouriteTvChannels = db.getChannel();
        listachannels=new LinkedList<String>();
        if(favouriteTvChannels!=null && favouriteTvChannels.size()>0){
        	
        	for(Channel c : favouriteTvChannels){
        			if(c.isFavourite()){
        				listachannels.add(c.getSigla());
        			}
		    }
        	
	        listview.setAdapter(new ImagemETextoListAdapter(this, listachannels, listview,R.layout.imagetexthome));
	        listview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					String sigla = ((ImagemETextoListAdapter)listview.getAdapter()).getItem(arg2);
					
					Intent intent = new Intent(FavouriteTV.this,Programs.class);
					intent.putExtra("sigla", sigla);
					startActivity(intent);
					
				}
			});
	        
        }
	}

    

	@Override
	public void onDestroy() {
		unbindService(mContextAlerterConnection);

		super.onDestroy();
	}

    
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==ID_FAVOURITE){
			favouriteTvChannels = db.getChannel();	
			listachannels.clear();
        	for(Channel c : favouriteTvChannels){
        			if(c.isFavourite()){
        				listachannels.add(c.getSigla());
        			}
		    }
        	listview.setAdapter(new ImagemETextoListAdapter(this, listachannels, listview,R.layout.imagetexthome));
        	listview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					String sigla = ((ImagemETextoListAdapter)listview.getAdapter()).getItem(arg2);
					
					Intent intent = new Intent(FavouriteTV.this,Programs.class);
					intent.putExtra("sigla", sigla);
					startActivity(intent);
					
				}
			});
	        
		}
	}


	private class ContextAlerterServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mContextAlerterService = ((ContextAlertBinder) service).getService();
			mContextAlerterService.refreshHome();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mContextAlerterService = null;
		}
	}


	@Override
	public void onClick(View v) {
		Intent i;
		if(v==btnHome){
			i = new Intent(this, Map.class);
	    	this.startActivity(i);
	        
		}else if(v==btnPrograms){
			i = new Intent(this, SelectChannels.class);
	        this.startActivityForResult(i, ID_FAVOURITE);
		}
		
	}

}