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
public abstract class Channels implements BaseColumns {

	public static final String NAME = "channels";
	public static final Uri CONTENT_URI = Uri.parse("content://" + FavouriteTvProvider.AUTHORITY + "/" + NAME);

	public static final String CHANNEL_ID = "_id";
	public static final String CHANNEL_NAME = "name";
	public static final String CHANNEL_SIGLA = "sigla";
	public static final String CHANNEL_FAVOURITE = "favourite";

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.favouritetv." + NAME;

	public static final String NAME_SINGLE = NAME + "/#";
	public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/vnd.favouritetv." + NAME;

}
