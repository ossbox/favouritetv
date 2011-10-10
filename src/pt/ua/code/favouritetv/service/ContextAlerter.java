/**
 * 
 */
package pt.ua.code.favouritetv.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import pt.ua.code.favouritetv.FavouriteTV;
import pt.ua.code.favouritetv.R;
import pt.ua.code.favouritetv.calendar.CalendarManager;
import pt.ua.code.favouritetv.content.Home;
import pt.ua.code.ws.Program;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * @author André Prata
 * 
 */
public class ContextAlerter extends Service {

	private static String TAG = "ContextAlerter";
	private LocationManager mLocationManager;
	private String mLocationProvider;
	private CalendarManager mCalendarManager;
	private Cursor c;
	// power management
	private BroadcastReceiver mLowPowerListener = new LowPowerHandler();
	private BroadcastReceiver mPowerOkListener = new PowerOkHandler();

	// home location and next program in calendar
	private double mHomeLatitude = 0.0, mHomeLongitude = 0.0;
	private double mLastKnownLatitude = 0.0, mLastKnownLongitude = 0.0;
	private Program mNextProgram = null;

	// notification
	private AlarmManager am;
	private Program lastNotified = null;
	private Program lastScheduleNotified = null;
	private Timer mNotificationTimer;
	private NotificationManager mNotificationManager;
	private Date notificationScheduledUntilProgramAt = Calendar.getInstance()
			.getTime();
	private int maxNotificationDistance, maxNotificationTime,
			minNotificationTime;
	private int gpsUpdatesMinTime, gpsUpdatesMinDistance;
	private String notificationInitialString, notificationMiddleString,
			notificationFinalString;
	private String lastID;
	private PendingIntent lastpendingIntent;
	/**
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return new ContextAlertBinder(this);
	}

	private void initializeVariables() {
		Log.d(TAG, "Initializing variables");
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Resources r = getResources();
		maxNotificationDistance = r
				.getInteger(R.integer.maxNotificationDistance);
		maxNotificationTime = r.getInteger(R.integer.maxNotificationTime);
		minNotificationTime = r.getInteger(R.integer.minNotificationTime);

		notificationInitialString = r
				.getString(R.string.notificationInitialString);
		notificationMiddleString = r
				.getString(R.string.notificationMiddleString);
		notificationFinalString = r.getString(R.string.notificationFinalString);

		gpsUpdatesMinDistance = r.getInteger(R.integer.gpsUpdatesMinDistance);
		gpsUpdatesMinTime = r.getInteger(R.integer.gpsUpdatesMinTime);

		mCalendarManager = new CalendarManager(this);

		Criteria locationUpdatesCriteria = new Criteria();
		locationUpdatesCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
		mLocationProvider = mLocationManager.getBestProvider(
				locationUpdatesCriteria, true);
		registerLocationUpdates();

		requestLowPowerNotification();
		requestPowerOkNotification();
	}

	/**
	 * Acquire a location manager and start listening for location updates.
	 * Doesn't need a lot of accuracy and updates are not very regular, it is
	 * mindful of phone resources!!
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Service creating");

		initializeVariables();

		registerHomeUpdates();
		registerProgramUpdates();

		refreshHome();
		refreshNextProgram();

		recalculateNotificationTime();
		
		Notification note=new Notification(R.drawable.channel_tv,
				"FavouriteTv is running.",
				System.currentTimeMillis());
		Intent i=new Intent(this, FavouriteTV.class);
		
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
		Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent pi=PendingIntent.getActivity(this, 0,
										i, 0);
		
		note.setLatestEventInfo(this, "FavouriteTV",
		"FavouriteTv is scheduling your notifications!",
		pi);
		note.flags|=Notification.FLAG_NO_CLEAR;
		
		startForeground(1337, note);

	}

	/**
	 * Stop using the GPS.
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "Service destroying");

		Log.d(TAG, "Unregister location updates");
		unregisterLocationUpdates();

		Log.d(TAG, "Unregister home location updates");
		unregisterHomeUpdates();

		Log.d(TAG, "Unregister program updates");
		unregisterProgramUpdates();

		Log.d(TAG, "Unregister power notifications");
		unregisterReceiver(mLowPowerListener);
		unregisterReceiver(mPowerOkListener);

		Log.d(TAG, "Service destroyed");
		if (!c.isClosed()) {
			c.close();
			c.deactivate();
		}
		stopForeground(true);
	}

	public void refreshPrograms() {
		notificationScheduledUntilProgramAt = Calendar.getInstance().getTime();
		refreshNextProgram();
		recalculateNotificationTime();
	}

	public void refreshHome() {
		Log.d(TAG, "Getting home location");

		c = getContentResolver()
				.query(Home.CONTENT_URI, null, null, null, null);
		if (c.moveToFirst()) {
			mHomeLatitude = (double) c.getInt(c.getColumnIndex(Home.LATITUDE)) / 1e6;
			mHomeLongitude = (double) c
					.getInt(c.getColumnIndex(Home.LONGITUDE)) / 1e6;

			Log.d(TAG, "New home location: " + mHomeLatitude + ", "
					+ mHomeLongitude);
		}

		if (!c.isClosed()) {
			c.close();
			c.deactivate();
		}
	}

	private void refreshNextProgram() {
		Log.d(TAG, "Getting next program");
		
		
		HashMap<Integer, String> activeCalendars = mCalendarManager
				.getActiveCalendars();
		if (activeCalendars.size() > 0) {
			List<Program> favouritePrograms = mCalendarManager
					.getAllFavouritePrograms(activeCalendars.keySet()
							.iterator().next());

			for (Program fav : favouritePrograms) {
				if ((mNextProgram == null && fav.getBegin().after(
						Calendar.getInstance().getTime()))
						|| (fav.getBegin().after(
								Calendar.getInstance().getTime()) && fav
								.getBegin().before(mNextProgram.getBegin()))) {
					mNextProgram = fav;
					
				}
			}
			
			if (favouritePrograms.size()==0 || (lastNotified != null && lastNotified.equals(mNextProgram)))
				mNextProgram = null;// nao foi actualizado esse ja foi
									// notificado
		}

		// mNextProgram = null;
	}

	private void registerHomeUpdates() {
		Log.d(TAG, "Registering home updates");

		ContentResolver cr = getContentResolver();
		cr.registerContentObserver(Home.CONTENT_URI, true, mHomeChangeObserver);
	}

	private void unregisterHomeUpdates() {
		Log.d(TAG, "Unregistering home updates");

		ContentResolver cr = getContentResolver();
		cr.unregisterContentObserver(mHomeChangeObserver);
	}

	private void registerProgramUpdates() {
		Log.d(TAG, "Registering program updates");

		ContentResolver cr = getContentResolver();
		cr.registerContentObserver(CalendarManager.getCalendarUri(), true,
				mProgramsChangeObserver);
	}

	private void unregisterProgramUpdates() {
		Log.d(TAG, "Unregistering program updates");

		ContentResolver cr = getContentResolver();
		cr.unregisterContentObserver(mProgramsChangeObserver);
	}

	private void registerLocationUpdates() {
		Log.d(TAG, "Subscribing location updates with provider: "
				+ mLocationProvider);

		mLocationManager.requestLocationUpdates(mLocationProvider,
				gpsUpdatesMinTime, gpsUpdatesMinDistance, locationListener);
	}

	private void unregisterLocationUpdates() {
		Log.d(TAG, "Removing location updates");

		mLocationManager.removeUpdates(locationListener);
	}

	
	private void recalculateNotificationTime() {
		Log.d(TAG, "Recalculating next notification");



		if (mNextProgram != null) {

			Log.d(TAG, "Program to notify found");
			float results[] = new float[1];
			Location.distanceBetween(mHomeLatitude, mHomeLongitude,
					mLastKnownLatitude, mLastKnownLongitude, results);

			int distance = (int) results[0];
			if (distance > maxNotificationDistance) {
				distance = maxNotificationDistance;
			}

			long alertBeforeProgram = minNotificationTime
					+ (distance * (maxNotificationTime - minNotificationTime))
					/ maxNotificationDistance;

			long when = mNextProgram.getBegin().getTime()
					- (alertBeforeProgram * 60 * 1000);
			
			removeAlarm(mNextProgram);
			
			Intent intent = new Intent(this, NotificationAlarm.class);
			intent.putExtra("ID",mNextProgram.getName()+"{" +mNextProgram.getId()+"}");
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(mNextProgram.getId()),
			    intent, PendingIntent.FLAG_ONE_SHOT);
			lastpendingIntent = pendingIntent;
			am.set(AlarmManager.RTC_WAKEUP,
					when, pendingIntent);
			
			Log.d(TAG, "Alarm set for program " + mNextProgram.getName() + " with begin at "
				+ mNextProgram.getBegin());
			
			lastNotified=mNextProgram;
			lastID = mNextProgram.getName()+"{"+mNextProgram.getId()+"}";
			mNextProgram = null;
			
		}
	}
	
	private void removeAlarm(Program program){
		if(lastID==null || lastpendingIntent==null)
			return;
		
		am.cancel(lastpendingIntent);
		Log.d(TAG, "Cancel alarm for program " + lastNotified.getName() + " with begin at "
				+ lastNotified.getBegin());
	}
	
	private void requestLowPowerNotification() {
		Log.d(TAG, "Requesting low power notification");

		IntentFilter lowPowerFilter = new IntentFilter();
		lowPowerFilter.addAction(Intent.ACTION_BATTERY_LOW);
		registerReceiver(mLowPowerListener, lowPowerFilter);
	}
	
	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
	    super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    super.onStartCommand(intent, flags, startId);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}
	
	private void requestPowerOkNotification() {
		Log.d(TAG, "Requesting power ok notification");

		IntentFilter powerOkFilter = new IntentFilter();
		powerOkFilter.addAction(Intent.ACTION_BATTERY_OKAY);
		registerReceiver(mPowerOkListener, powerOkFilter);
	}

	protected LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location loc) {
			Log.d(TAG, "Detected current location change");
			mLastKnownLatitude = loc.getLatitude();
			mLastKnownLongitude = loc.getLongitude();

			Log.d(TAG, "New current location: " + mLastKnownLatitude + ", "
					+ mLastKnownLongitude);
			refreshNextProgram();
			recalculateNotificationTime();
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "Location provider disabled");

			unregisterLocationUpdates();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "Location provider enabled");

			registerLocationUpdates();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// do nothing
		}
	};

	protected ContentObserver mProgramsChangeObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, "Detected home change");
			refreshNextProgram();
			recalculateNotificationTime();
		}
	};

	protected ContentObserver mHomeChangeObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			Log.d(TAG, "Detected home change");
			refreshHome();
			recalculateNotificationTime();
		}
	};

	class LowPowerHandler extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			unregisterLocationUpdates();
		}
	}

	class PowerOkHandler extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			registerLocationUpdates();
		}
	}
}
