package pt.ua.code.favouritetv.service;

import java.util.HashMap;

import pt.ua.code.favouritetv.R;
import pt.ua.code.favouritetv.calendar.CalendarManager;
import pt.ua.code.ws.Program;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

public class NotificationAlarm extends BroadcastReceiver {
	private NotificationManager mNotificationManager;
	private int maxNotificationDistance, maxNotificationTime,
	minNotificationTime;
	private final String TAG = "NotificationAlarm";
	private String notificationInitialString, notificationMiddleString,
		notificationFinalString;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Init Alarm");
		
		Resources r = context.getResources();
		maxNotificationDistance = r
				.getInteger(R.integer.maxNotificationDistance);
		maxNotificationTime = r.getInteger(R.integer.maxNotificationTime);
		minNotificationTime = r.getInteger(R.integer.minNotificationTime);

		notificationInitialString = r
				.getString(R.string.notificationInitialString);
		notificationMiddleString = r
				.getString(R.string.notificationMiddleString);
		notificationFinalString = r.getString(R.string.notificationFinalString);
		
		mNotificationManager = (NotificationManager) context
	    .getSystemService(Context.NOTIFICATION_SERVICE);
		String ID = intent.getStringExtra("ID");
		if(ID==null){
			Log.d(TAG, "Program ID is Null");
			return;
		}
		CalendarManager cm = new CalendarManager(context);
		HashMap<Integer, String> activecalendars = cm.getActiveCalendars();
		if (activecalendars == null || activecalendars.isEmpty()) {
			Log.d(TAG, "No callendar found!");
			return;
		}
		int cal_id = activecalendars.keySet().iterator().next();
		Program mNextProgram = cm.getProgram(cal_id,ID);
		if(mNextProgram==null){
			Log.d(TAG, "No program found!");
			return;
		}
		Log.d(TAG, "Creating Alarm");
		int minutesDifference = (int) ((mNextProgram.getBegin().getTime() - System
				.currentTimeMillis()) / (60 * 1000));
		
		CharSequence contentText = notificationInitialString + " "
				+ mNextProgram.getName() + " " + notificationMiddleString + " "
				+ minutesDifference + " " + notificationFinalString;
		
		Notification alert = new Notification(R.drawable.channel_tv,
				contentText, System.currentTimeMillis());
		alert.defaults |= Notification.DEFAULT_SOUND;
		alert.defaults |= Notification.DEFAULT_VIBRATE;
		alert.ledARGB = 0xff00ff00;
		alert.ledOnMS = 300;
		alert.ledOffMS = 1000;
		alert.flags |= Notification.FLAG_SHOW_LIGHTS;

		CharSequence contentTitle = "FavouriteTV";
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(), 0);

		alert.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		Log.d(TAG, "Sending Alarm");
		mNotificationManager.notify(1, alert);
		Log.d(TAG, "Removing program from calendar.");
		cm.removeProgram(mNextProgram, cal_id);

	}

}
