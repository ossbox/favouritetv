/**
 * 
 */
package pt.ua.code.favouritetv.content;

import java.util.LinkedList;

import pt.ua.code.ws.Channel;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This Helper handles all tables. You can't have different helpers for the same
 * database, and it doesn't make sense to use one database to store the user's
 * home location.
 * 
 * @author André Prata
 * @author Eriksson Monteiro
 */
public class SQLiteFavouriteTvHelper extends SQLiteOpenHelper {

	private static final String TAG = "SQLiteChannelHelper";
	private static final String DATABASE_NAME = "favouritetv.db";
	private static final int DATABASE_VERSION = 2;
	
	public SQLiteFavouriteTvHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	
	}

	/**
	 * @see SQLiteOpenHelper#onCreate(SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Creating database: " + DATABASE_NAME + " version: " + DATABASE_VERSION);

		Log.d(TAG, "Creating table: " + Channels.NAME);
		String sql = "create table " + Channels.NAME + " ( " + Channels.CHANNEL_ID
				+ " integer primary key autoincrement, " + Channels.CHANNEL_FAVOURITE + " integer not null, "
				+ Channels.CHANNEL_SIGLA + " text unique not null, " + Channels.CHANNEL_NAME + " text not null );";
		db.execSQL(sql);

		Log.d(TAG, "Creating table: " + Home.NAME);
		sql = "create table " + Home.NAME + " ( " + Home.HOME_ID + " integer primary key autoincrement, "
				+ Home.LATITUDE + " integer not null, " + Home.LONGITUDE + " integer not null );";
		db.execSQL(sql);
	}

	/**
	 * @see SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// currently nothing
		
	}

	public boolean insertChannel(Channel channel){
		if(channel == null || channel.getName()==null || channel.getSigla()==null)
			return false;
		
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
       
        values.put(Channels.CHANNEL_FAVOURITE, channel.isFavourite()?1:0);
        values.put(Channels.CHANNEL_SIGLA, channel.getSigla());
        values.put(Channels.CHANNEL_NAME, channel.getName());
        db.insert(Channels.NAME, null, values);
		db.close();
		return true;
	}
	
	public LinkedList<Channel> getChannel(){
		LinkedList<Channel> channels = new LinkedList<Channel>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(Channels.NAME, null, null, null, null, null, null);
		
		while (cursor.moveToNext()) {
			boolean favourite = cursor.getInt(1)==1 ? true : false;
			String sigla = cursor.getString(2);
			String name = cursor.getString(3);
			channels.add(new Channel(name,sigla,favourite));
	    }
		cursor.close();
		cursor.deactivate();
		db.close();
		return channels;
	}
	
	public boolean setFavourite(String sigla, int favourite){
		if(sigla==null)
			return false;
		
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("update "+Channels.NAME+" set "+Channels.CHANNEL_FAVOURITE+"=? where "+Channels.CHANNEL_SIGLA+"=?", new String[]{""+favourite,sigla});
		db.close();
		return true;
	}
	
	public Channel getChannelBySigla(String sigla){
		Channel channel = new Channel();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(Channels.NAME, null, Channels.CHANNEL_SIGLA+"=?", new String[]{sigla}, null, null, null);
		
		while (cursor.moveToNext()) {
			boolean favourite = cursor.getInt(1)==1 ? true : false;
			String name = cursor.getString(3);
			channel = new Channel(name,sigla,favourite);
	    }
		
		cursor.close();
		cursor.deactivate();
		db.close();
		return channel;
	}
}
