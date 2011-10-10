package pt.ua.code.favouritetv;

import java.util.ArrayList;
import java.util.List;

import pt.ua.code.favouritetv.content.Home;
import pt.ua.code.favouritetv.gui.MyMap;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Map extends MapActivity implements OnGestureListener, OnDoubleTapListener, OnClickListener{
	private MyMap mapView;
	private HomeItemizedOverlay itemizedoverlay;
	private MyOverlayItem overlayHome;
	private LocationManager mlocManager;
	private Button btnMapsConfig, btnGpsOptions;
	
	
	LocationListener mlocListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			
			GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
			overlayHome = new MyOverlayItem(p, getString(R.string.Casa),getString(R.string.HelloCasa));
			itemizedoverlay.addOverlay(overlayHome);
			ContentValues initialValues = new ContentValues();
			initialValues.put(Home.LATITUDE, p.getLatitudeE6());
			initialValues.put(Home.LONGITUDE, p.getLongitudeE6());
			getContentResolver().insert(Home.CONTENT_URI, initialValues);
			mapView.invalidate();
			mapView.getController().animateTo(p);
			Toast.makeText(Map.this, "Localização Gps", Toast.LENGTH_SHORT).show();
		}
	};
	

	protected void onPause() {
		mlocManager.removeUpdates(mlocListener);
		super.onPause();
	};
	
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		this.setContentView(R.layout.map);
		btnGpsOptions = (Button) findViewById(R.id.mapa_gps);
		btnMapsConfig = (Button) findViewById(R.id.mapa_maps_conf);
		
		registerForContextMenu(btnGpsOptions);
		registerForContextMenu(btnMapsConfig);
		btnGpsOptions.setOnClickListener(this);
		btnMapsConfig.setOnClickListener(this);
		
		mapView = (MyMap) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.home);
		itemizedoverlay = new HomeItemizedOverlay(drawable, this);
		GeoPoint point;
		Cursor  c = getContentResolver().query(Home.CONTENT_URI, null, null, null, null);
		if(c.moveToFirst()){
			point = new GeoPoint(c.getInt(c.getColumnIndex(Home.LATITUDE)), c.getInt(c.getColumnIndex(Home.LONGITUDE)));
			overlayHome = new MyOverlayItem(point, getString(R.string.Casa), getString(R.string.HelloCasa));
		}else{
			point = new GeoPoint(19240000, -99120000);
			overlayHome = new MyOverlayItem(point, getString(R.string.Casa), getString(R.string.HelloCasa));
		}
		c.close();
		c.deactivate();
		itemizedoverlay.addOverlay(overlayHome);
		mapOverlays.add(itemizedoverlay);
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mapView.getController().animateTo(point);
		mapView.getController().setZoom(19);
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.mapamenu, menu);
//		return true;
//	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v==btnGpsOptions){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.mapagps, menu);
			if(gps){
				menu.findItem(R.id.itemDesligarGPS).setChecked(false);
				menu.findItem(R.id.itemLigarGps).setChecked(true);
			}
		}else if(v==btnMapsConfig){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.mapamenu, menu);
			if(satelite){
				menu.findItem(R.id.itemEstrada).setChecked(false);
				menu.findItem(R.id.itemSatelite).setChecked(true);
			}
		}
	}
	
	private boolean gps=false, satelite=false;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemLigarGps:
			mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 4000, 10, mlocListener);
			Location location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
			overlayHome = new MyOverlayItem(p, getString(R.string.Casa),getString(R.string.HelloCasa));
			itemizedoverlay.addOverlay(overlayHome);
			ContentValues initialValues = new ContentValues();
			initialValues.put(Home.LATITUDE, p.getLatitudeE6());
			initialValues.put(Home.LONGITUDE, p.getLongitudeE6());
			getContentResolver().insert(Home.CONTENT_URI, initialValues);
			mapView.invalidate();
			mapView.getController().animateTo(p);
			gps=true;
			return true;
		case R.id.itemDesligarGPS:
			mlocManager.removeUpdates(mlocListener);
			gps=false;
			return true;
		case R.id.itemSatelite:
			mapView.setSatellite(true);
			mapView.setStreetView(false);
			satelite=true;
			return true;
		case R.id.itemEstrada:
			mapView.setSatellite(false);
			mapView.setStreetView(true);
			satelite=false;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle item selection
//		 if (item.isChecked()) item.setChecked(false);
//		 else item.setChecked(true);
//		    
//		switch (item.getItemId()) {
//		case R.id.itemLigarGps:
//			mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 4000, 10, mlocListener);
//			Location location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
//			overlayHome = new MyOverlayItem(p, getString(R.string.Casa),getString(R.string.HelloCasa));
//			itemizedoverlay.addOverlay(overlayHome);
//			ContentValues initialValues = new ContentValues();
//			initialValues.put(Home.LATITUDE, p.getLatitudeE6());
//			initialValues.put(Home.LONGITUDE, p.getLongitudeE6());
//			getContentResolver().insert(Home.CONTENT_URI, initialValues);
//			mapView.invalidate();
//			mapView.getController().animateTo(p);
//			return true;
//		case R.id.itemDesligarGPS:
//			mlocManager.removeUpdates(mlocListener);
//			return true;
//		case R.id.itemSatelite:
//			mapView.setSatellite(true);
//			mapView.setStreetView(false);
//			return true;
//		case R.id.itemEstrada:
//			mapView.setSatellite(false);
//			mapView.setStreetView(true);
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private class MyOverlayItem extends OverlayItem {

		public MyOverlayItem(GeoPoint point, String title, String snippet) {
			super(point, title, snippet);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o instanceof OverlayItem) {
				return this.getTitle().compareTo(((OverlayItem) o).getTitle()) == 0;
			}
			return false;
		}
	}

	private class HomeItemizedOverlay extends ItemizedOverlay<OverlayItem> {

		private ArrayList<MyOverlayItem> mOverlays = new ArrayList<MyOverlayItem>();
		private Context context;

		public HomeItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			this.context = context;
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		public void addOverlay(MyOverlayItem overlay) {
			if (mOverlays.contains(overlay))
				mOverlays.remove(overlay);
			mOverlays.add(overlay);
			populate();
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			return true;
		}

		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			return super.onTap(p, mapView);
		}
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		GeoPoint p = mapView.getProjection().fromPixels((int)e.getX(), (int)e.getY());
		overlayHome = new MyOverlayItem(p, getString(R.string.Casa),getString(R.string.HelloCasa));
		itemizedoverlay.addOverlay(overlayHome);
		ContentValues initialValues = new ContentValues();
		initialValues.put(Home.LATITUDE, p.getLatitudeE6());
		initialValues.put(Home.LONGITUDE, p.getLongitudeE6());
		getContentResolver().insert(Home.CONTENT_URI, initialValues);
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		v.showContextMenu();
	}

}
