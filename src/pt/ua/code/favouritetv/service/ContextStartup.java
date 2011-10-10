/**
 * 
 */
package pt.ua.code.favouritetv.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author André Prata
 *
 */
public class ContextStartup extends BroadcastReceiver {
	private static String TAG = "ContextStartup";

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Starting the ContextAlerter service.");

		Intent serviceIntent = new Intent();
		serviceIntent.setAction("pt.ua.code.favouritetv.service.ContextAlerter");
		context.startService(serviceIntent);

		Log.d(TAG, "ContextAlerter started.");
	}
}
