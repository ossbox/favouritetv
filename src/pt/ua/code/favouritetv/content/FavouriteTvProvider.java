/**
 * 
 */
package pt.ua.code.favouritetv.content;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author André Prata
 * 
 */
public class FavouriteTvProvider extends ContentProvider {

	public static final String AUTHORITY = "pt.ua.code.favouritetv.content.FavouriteTvProvider";

	private static final int HOME = 1;
	private static final int CHANNELS = 2;
	private static final int CHANNEL_ID = 3;

	private SQLiteOpenHelper mDatabaseHelper;
	private static final UriMatcher mUriMatcher;
	private static final Map<String, String> homeProjectionMap;
	private static final Map<String, String> channelsProjectionMap;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, Home.NAME, HOME);
		mUriMatcher.addURI(AUTHORITY, Channels.NAME, CHANNELS);
		mUriMatcher.addURI(AUTHORITY, Channels.NAME_SINGLE, CHANNEL_ID);

		homeProjectionMap = new HashMap<String, String>(3);
		homeProjectionMap.put(Home.HOME_ID, Home.HOME_ID);
		homeProjectionMap.put(Home.LATITUDE, Home.LATITUDE);
		homeProjectionMap.put(Home.LONGITUDE, Home.LONGITUDE);

		channelsProjectionMap = new HashMap<String, String>(4);
		channelsProjectionMap.put(Channels.CHANNEL_ID, Channels.CHANNEL_ID);
		channelsProjectionMap.put(Channels.CHANNEL_NAME, Channels.CHANNEL_NAME);
		channelsProjectionMap.put(Channels.CHANNEL_SIGLA, Channels.CHANNEL_SIGLA);
		channelsProjectionMap.put(Channels.CHANNEL_FAVOURITE, Channels.CHANNEL_FAVOURITE);
	};

	/**
	 * 
	 * @see ContentProvider#delete(Uri, String, String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int count = 0;

		switch (mUriMatcher.match(uri)) {
		case HOME:
			count = db.delete(Home.NAME, where, whereArgs);
			break;

		case CHANNELS:
			count = db.delete(Channels.NAME, where, whereArgs);
			break;

		case CHANNEL_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(Channels.NAME, Channels.CHANNEL_ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			count = db.delete(Channels.NAME, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	/**
	 * 
	 * @see ContentProvider#getType(Uri)
	 */
	@Override
	public String getType(Uri uri) {
		String type;
		switch (mUriMatcher.match(uri)) {
		case HOME:
			type = Home.CONTENT_TYPE;
			break;

		case CHANNELS:
			type = Channels.CONTENT_TYPE;
			break;

		case CHANNEL_ID:
			type = Channels.CONTENT_TYPE_SINGLE;
			break;

		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}

		return type;
	}

	/**
	 * Returns null if insertion failed.
	 * 
	 * @see ContentProvider#insert(Uri, ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Uri _uri = null;
		long id = 0;

		switch (mUriMatcher.match(uri)) {
		case HOME:
			Cursor c = db.query(Home.NAME, new String[]{Home.HOME_ID}, null, null, null, null, null);
			if(c.moveToFirst()){
				int home_id = c.getInt(c.getColumnIndex(Home.HOME_ID));
				db.update(Home.NAME, initialValues, Home.HOME_ID+"="+home_id, null);
				_uri= ContentUris.withAppendedId(Home.CONTENT_URI, home_id);
			}else{
				if ((id = db.insert(Home.NAME, "", initialValues)) > 0) {
					_uri = ContentUris.withAppendedId(Home.CONTENT_URI, id);
				}
			}
			c.close();
			c.deactivate();
			break;

		case CHANNELS:
			if ((id = db.insert(Home.NAME, "", initialValues)) > 0) {
				_uri = ContentUris.withAppendedId(Home.CONTENT_URI, id);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown/illegal uri: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return _uri;
	}

	/**
	 * 
	 * @see ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		
		mDatabaseHelper = new SQLiteFavouriteTvHelper(getContext());
		Log.i("ContentProvider","Created.");
		return true;
	}

	/**
	 * 
	 * @see ContentProvider#query(Uri, String[], String, String[], String)
	 */
	@Override
	public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (mUriMatcher.match(uri)) {
		case HOME:
			qb.setTables(Home.NAME);
			qb.setProjectionMap(homeProjectionMap);
			break;

		case CHANNEL_ID:
			qb.appendWhere(Channels.CHANNEL_ID + "=" + uri.getPathSegments().get(1));
			// break; //intentionally omitted!

		case CHANNELS:
			qb.setTables(Channels.NAME);
			qb.setProjectionMap(channelsProjectionMap);
			break;

		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}

		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	/**
	 * 
	 * @see ContentProvider#update(Uri, ContentValues, String, String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		int count = 0;

		switch (mUriMatcher.match(uri)) {
		case HOME:
			count = db.update(Home.NAME, values, where, whereArgs);
			break;

		case CHANNELS:
			count = db.update(Home.NAME, values, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown/illegal: " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

}
