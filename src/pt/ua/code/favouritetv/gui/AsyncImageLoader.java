package pt.ua.code.favouritetv.gui;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import pt.ua.code.favouritetv.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageLoader {

	private static HashMap<String, SoftReference<Bitmap>> imageCache;
	private static final AsyncImageLoader instance = new AsyncImageLoader();
	private static final Thread controler[] = new Thread[8];
	private static int threadCount = 0;
	private static final List<String> receivingUrlsImage = new LinkedList<String>();

	private AsyncImageLoader() {
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	public static final AsyncImageLoader getInstance() {
		return instance;
	}

	public boolean isReceivingImage(String imageUrl) {
		return receivingUrlsImage.contains(imageUrl);
	}

	public static Bitmap getImageInCache(String imageUrl) {
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
			Bitmap drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}
		return null;
	}

	public Bitmap loadDrawableFromResources(final Context context, final String imageUrl, final String res,
			final ImageCallback imageCallback) {

		Bitmap tmp = null;
		if ((tmp = getImageInCache(imageUrl)) != null) {
			return tmp;
		}

		receivingUrlsImage.add(imageUrl);

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageCallback.imageLoaded(((Bitmap) message.obj), imageUrl);
				receivingUrlsImage.remove(imageUrl);
			}
		};

		if (controler[threadCount] != null) {
			try {
				if (controler[threadCount] != null)
					controler[threadCount].join();
				if (controler[threadCount] != null)
					controler[threadCount].destroy();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			controler[threadCount] = null;
		}

		controler[threadCount] = new Thread() {
			private String localUrl;

			@Override
			public void run() {
				localUrl = imageUrl;
				Bitmap drawable = loadImageFromResources(context, res);
				imageCache.put(imageUrl, new SoftReference<Bitmap>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}

			@Override
			public void destroy() {
				// TODO Auto-generated method stub
				receivingUrlsImage.remove(localUrl);
			}

		};

		controler[threadCount].setPriority(Thread.MIN_PRIORITY);
		controler[threadCount].start();
		threadCount = (threadCount + 1) % controler.length;
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void dropOneCacheEntry() {
		try {
			Iterator<Entry<String, SoftReference<Bitmap>>> it = imageCache.entrySet().iterator();
			Object tmp;
			String key = null;

			if (it.hasNext()) {

				tmp = it.next();
				if (tmp != null) {
					key = ((Entry<String, SoftReference<Bitmap>>) tmp).getKey();

					tmp = ((Entry<String, SoftReference<Bitmap>>) tmp).getValue();

				}
				if (tmp != null)
					tmp = ((Reference<Bitmap>) tmp).get();

				if (tmp != null)
					if (tmp instanceof Bitmap) {
						// ((Bitmap) tmp).recycle();
						tmp = null;
						Log.i("Recycling", "One bitmap Recycled");
					}

				imageCache.remove(key);
			}

		} catch (ConcurrentModificationException e) {

		}
	}

	public static Bitmap loadImageFromResources(Context context, String resource) {

		Bitmap ret = null;
		Options opts = new Options();
		opts.inPurgeable = true;
		opts.inPreferredConfig = Config.ARGB_8888;
		Resources res = context.getResources();
		try {

			ret = BitmapFactory.decodeResource(res,
					res.getIdentifier("channel_" + formatResouceName(resource), "drawable", context.getPackageName()),
					opts);
			if (ret == null) {
				ret = BitmapFactory.decodeResource(res, R.drawable.channel_demo, opts);
			}

		} catch (OutOfMemoryError error) {
			for (int i = 0; i < 6; i++) {
				Log.i("Recycling", "One bitmap Recycled because of memory");
				dropOneCacheEntry();
			}
			System.gc();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Resources.NotFoundException e) {
			ret = BitmapFactory.decodeResource(res, R.drawable.channel_demo, opts);
		}
		return ret;
	}

	private static String formatResouceName(String sigla) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < sigla.length(); i++) {
			char c = sigla.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '.') {
				result.append(c);
			}
		}
		return result.toString().toLowerCase();
	}

	public final void stopLoadingImages() {
		for (int i = 0; i < threadCount; i++) {
			if (controler[i] != null) {
				// try {
				controler[i].interrupt();
				// controler[i].join();
				// } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				controler[i] = null;
			}

		}
		System.gc();
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageDrawable, String imageTag);
	}
}
