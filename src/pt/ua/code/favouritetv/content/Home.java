/**
 * 
 */
package pt.ua.code.favouritetv.content;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author André Prata
 * 
 */
public abstract class Home implements BaseColumns {

	public static final String NAME = "home";
	public static final Uri CONTENT_URI = Uri.parse("content://" + FavouriteTvProvider.AUTHORITY + "/" + NAME);

	public static final String HOME_ID = "_id";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd.favouritetv." + NAME;

}
