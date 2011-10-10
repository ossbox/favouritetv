package pt.ua.code.favouritetv.calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import pt.ua.code.ws.Program;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

public class CalendarManager {
	private final Context mContext;
	private final static Uri CALENDAR_CALENDARS_URI = Uri.parse("content://calendar/calendars");
	private final static Uri CALENDAR_CALENDARS_URI_8_PLUS = Uri.parse("content://com.android.calendar/calendars");
	private final static Uri CALENDAR_EVENTS_URI = Uri.parse("content://calendar/events");
	private final static Uri CALENDAR_EVENTS_URI_8_PLUS = Uri.parse("content://com.android.calendar/events");
	private NotificationManager mNotificationManager;
	
	public static Uri getCalendarUri() {
	    if(Integer.parseInt(Build.VERSION.SDK) <= 7) {
	        return CALENDAR_CALENDARS_URI;
	    } else {
	        return CALENDAR_CALENDARS_URI_8_PLUS;
	    }
	}
	
	public static Uri getEventsUri() {
	    if(Integer.parseInt(Build.VERSION.SDK) <= 7) {
	        return CALENDAR_EVENTS_URI;
	    } else {
	        return CALENDAR_EVENTS_URI_8_PLUS;
	    }
	}
	
	public CalendarManager(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}
	
	public HashMap<Integer,String> getActiveCalendars(){
		HashMap<Integer,String> lcalendar = new HashMap<Integer, String>();
		String[] projection = new String[] { "_id", "name" };
		Uri calendars = getCalendarUri();
		Cursor managedCursor = null;
		try{
			managedCursor =
			  mContext.getContentResolver().query(calendars, projection, "selected=1", null, null);
			if(managedCursor.moveToFirst()){
				int nameColumn = managedCursor.getColumnIndex("name"); 
				int idColumn = managedCursor.getColumnIndex("_id");
				do{
					lcalendar.put(managedCursor.getInt(idColumn), managedCursor.getString(nameColumn));
				}while(managedCursor.moveToNext());
			}
		}finally{
			if(managedCursor!=null && !managedCursor.isClosed()){
				managedCursor.close();
				managedCursor.deactivate();
			}
		}
		return lcalendar;
	}
	
	public void addProgram(Program p, int calendar_id){
		ContentValues event = new ContentValues();
		event.put("calendar_id", calendar_id);
		event.put("title", p.getName()+"{"+p.getId()+"}");
		//event.put("_id", p.getId());
		//event.put("description", p.getDescription());
		event.put("eventLocation", "FavouriteTV");
		try{
			long startTime = p.getBegin().getTime();
			long endTime = p.getEnd().getTime();
			event.put("dtstart", startTime);
			event.put("dtend", endTime);
		}catch(NullPointerException ex){
			return;
		}
		
		event.put("eventStatus", 0);//tentative (0), confirmed (1) or canceled (2):
		event.put("hasAlarm", 1); // 0 for false, 1 for true
		Uri eventsUri = getEventsUri();
		mContext.getContentResolver().insert(eventsUri, event);
	}
	
	public void removeProgram(Program p, int calendar_id){
		String where = "calendar_id="+calendar_id+" and title=?" +
				" and eventLocation='FavouriteTV' and dtstart=? and dtend=?";
		String[] args = new String[]{
				p.getName()+"{"+p.getId()+"}",
				""+p.getBegin().getTime(), ""+p.getEnd().getTime()
		};
		mContext.getContentResolver().delete(getEventsUri(), where, args);
		mNotificationManager.cancel(Integer.parseInt(p.getId()));
	}
	
	public List<Program> getAllFavouritePrograms(int calendar_id){
		LinkedList<Program> programs = new LinkedList<Program>();
		Cursor managedCursor = mContext.getContentResolver().query(getEventsUri(), null, "calendar_id="+calendar_id+" and eventLocation='FavouriteTV'", null, null);
		if(managedCursor.moveToFirst()){
			int title = managedCursor.getColumnIndex("title"); 
			//int description = managedCursor.getColumnIndex("description");
			int start = managedCursor.getColumnIndex("dtstart");
			int end= managedCursor.getColumnIndex("dtend");
			//int categories = managedCursor.getColumnIndex("_id");
			
			
			do{
				
				Program p = new Program();
				String name = managedCursor.getString(title);
				if(name.lastIndexOf("{")<0){
					continue;
				}
				p.setName(name.substring(0, name.lastIndexOf("{")));
				//p.setDescription(managedCursor.getString(description));
				p.setBegin(new Date(managedCursor.getLong(start)));
				p.setEnd(new Date(managedCursor.getLong(end)));
				String id;
				try{
					id = name.substring(name.lastIndexOf("{")+1,name.lastIndexOf("}"));
					Integer.parseInt(id);
				}catch(NumberFormatException ex){
					continue;
				}
				p.setId(id);
				p.setFavourite(true);
				programs.add(p);
				
			}while(managedCursor.moveToNext());
		}
		managedCursor.close();
		managedCursor.deactivate();
		return programs;
	}
	
	public Program getProgram(int calendar_id, String _id_program){
		Program program = null;
		String[] args = new String[]{
				_id_program
		};
		Cursor managedCursor = mContext.getContentResolver().query(getEventsUri(), null, "calendar_id="+calendar_id+" and eventLocation='FavouriteTV' and title=?", args, null);
		if(managedCursor.moveToFirst()){
			int title = managedCursor.getColumnIndex("title"); 
			//int description = managedCursor.getColumnIndex("description");
			int start = managedCursor.getColumnIndex("dtstart");
			int end= managedCursor.getColumnIndex("dtend");
			//int categories = managedCursor.getColumnIndex("_id");
				
			Program p = new Program();
			String name = managedCursor.getString(title);
			
			p.setName(name.substring(0, name.lastIndexOf("{")));
			//p.setDescription(managedCursor.getString(description));
			p.setBegin(new Date(managedCursor.getLong(start)));
			p.setEnd(new Date(managedCursor.getLong(end)));
			String id = null;
			try{
				id = name.substring(name.lastIndexOf("{")+1,name.lastIndexOf("}"));
				Integer.parseInt(id);
			}catch(NumberFormatException ex){
				return program;
			}
			p.setId(id);
			p.setFavourite(true);
			program = p;
		}
		managedCursor.close();
		managedCursor.deactivate();
		return program;
	}
}
