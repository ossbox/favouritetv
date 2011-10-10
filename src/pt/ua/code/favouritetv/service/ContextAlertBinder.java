package pt.ua.code.favouritetv.service;

import java.lang.ref.WeakReference;

import android.os.Binder;

public class ContextAlertBinder extends Binder {
	/**
	 * 
	 */
	private final WeakReference<ContextAlerter> mService;

	/**
	 * @param contextAlerter
	 */
	ContextAlertBinder(ContextAlerter service) {
		mService = new WeakReference<ContextAlerter>(service);
	}

	public ContextAlerter getService() {
		return mService.get();
	}
}