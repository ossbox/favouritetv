package pt.ua.code.favouritetv.gui;

import java.util.HashMap;
import java.util.List;

import pt.ua.code.favouritetv.FavouriteTV;
import pt.ua.code.favouritetv.R;
import pt.ua.code.favouritetv.calendar.CalendarManager;
import pt.ua.code.favouritetv.content.SQLiteFavouriteTvHelper;
import pt.ua.code.favouritetv.gui.AsyncImageLoader.ImageCallback;
import pt.ua.code.ws.Channel;
import pt.ua.code.ws.Program;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class ImagemETextoListAdapter extends ArrayAdapter<String> {

	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	private SQLiteFavouriteTvHelper db;
	private final HashMap<String, Integer> ratingValue = new HashMap<String, Integer>();
	private final HashMap<Integer, Integer> programRatinValue = new HashMap<Integer, Integer>();
	private final int layout;
	private List<String> imgresname;
	private List<String> programHour;
	private List<Program> favouritePrograms;
	private List<Program> programs;
	private CalendarManager cm;
	private Program p = new Program();

	public ImagemETextoListAdapter(Activity activity,
			List<String> imageAndTexts, ListView listView, int layout) {
		super(activity, 0, imageAndTexts);

		this.listView = listView;
		this.layout = layout;
		asyncImageLoader = AsyncImageLoader.getInstance();
		db = new SQLiteFavouriteTvHelper(this.getContext());
		this.cm = new CalendarManager(this.getContext());
	}

	public ImagemETextoListAdapter(Activity activity,
			List<String> imageAndTexts, ListView listView, int layout,
			List<String> imgresourcename, List<String> programshour,
			List<Program> programs) {
		super(activity, 0, imageAndTexts);
		if (imageAndTexts.size() != imgresourcename.size()
				|| imageAndTexts.size() != programshour.size()
				|| imageAndTexts.size() != programs.size()) {
			throw new RuntimeException("List size mismatch");
		}

		this.listView = listView;
		this.layout = layout;
		asyncImageLoader = AsyncImageLoader.getInstance();
		db = new SQLiteFavouriteTvHelper(this.getContext());
		this.imgresname = imgresourcename;
		this.programHour = programshour;
		this.cm = new CalendarManager(this.getContext());
		HashMap<Integer, String> activecalendars = cm.getActiveCalendars();
		this.programs = programs;
		if (activecalendars == null || activecalendars.isEmpty()) {
			Toast.makeText(
					this.getContext(),
					"Não é possível adicionar programas ao calendário! Nenhum calendário está disponível!",
					Toast.LENGTH_LONG).show();
			return;
		}
		this.favouritePrograms = cm.getAllFavouritePrograms(activecalendars
				.keySet().iterator().next());

	}

	public Integer getRatingValue(String id) {
		return ratingValue.get(id);
	}

	public void setRatingValue(String id, Integer rate) {
		ratingValue.put(id, rate);
	}

	public Integer getRatingValue(Integer id) {
		return programRatinValue.get(id);
	}

	public void setRatingValue(Integer id, Integer rate) {
		programRatinValue.put(id, rate);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();

		View rowView = convertView;
		final ViewCache viewCache;

		if (rowView != null) {
			// ja esta em cache
			viewCache = (ViewCache) rowView.getTag();
		} else {
			// se nao foi criando ainda
			LayoutInflater inflater = activity.getLayoutInflater();
			if (layout == R.layout.imagetext) {
				rowView = inflater.inflate(layout, null);
				viewCache = new ViewCache(rowView);
				rowView.setTag(viewCache);

				final RatingBar r = (RatingBar) rowView
						.findViewById(R.id.ratingfavourite);
				r.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (((RatingBar) v) == r) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								String ratebtn = (String) ((RatingBar) v)
										.getTag();
								int rate = ratingValue.get(ratebtn);
								if (rate == r.getRating()) {
									if (rate == 0) {
										r.setRating(1);
										db.setFavourite(viewCache.getTextView()
												.getText().toString(), 1);
										ratingValue.put(ratebtn, 1);
									} else {
										r.setRating(0);
										db.setFavourite(viewCache.getTextView()
												.getText().toString(), 0);
										ratingValue.put(ratebtn, 0);
									}
								}
							}
						}
						return true;
					}
				});

			} else if (layout == R.layout.imagetextprograms) {
				rowView = inflater.inflate(layout, null);
				viewCache = new ViewCache(rowView);
				rowView.setTag(viewCache);
				TextView t = (TextView) rowView.findViewById(R.id.idTextView);
				t.setTextSize(13);
				final RatingBar r = (RatingBar) rowView
						.findViewById(R.id.ratingfavourite);
				r.setOnTouchListener(new OnTouchListener() {
					private int id = position;

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (((RatingBar) v) == r) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								Integer ratebtn = (Integer) ((RatingBar) v)
										.getTag();
								int rate = programRatinValue.get(ratebtn);
								HashMap<Integer, String> activecalendars = cm
										.getActiveCalendars();
								if (rate == r.getRating()) {
									if (rate == 0) {
										if (activecalendars == null
												|| activecalendars.isEmpty()) {
											Toast.makeText(
													getContext(),
													"Não é possível adicionar programas ao calendário! Nenhum calendário está disponível!",
													Toast.LENGTH_LONG).show();
											return true;
										}
										r.setRating(1);
										cm.addProgram(programs.get(id),
												activecalendars.keySet()
														.iterator().next());
										programRatinValue.put(ratebtn, 1);
									} else {
										if (activecalendars == null
												|| activecalendars.isEmpty()) {
											Toast.makeText(
													getContext(),
													"NÃ£o Ã© possivel addicionar ao calendÃ¡rio! Nenhum calendario estÃ¡ disponÃ­vel!",
													Toast.LENGTH_LONG).show();
											return true;
										}
										r.setRating(0);
										cm.removeProgram(programs.get(id),
												activecalendars.keySet()
														.iterator().next());
										programRatinValue.put(ratebtn, 0);
									}
								}
								FavouriteTV.getContextAlerterService()
										.refreshPrograms();
							}
						}
						return true;
					}
				});

			} else if (layout == R.layout.imagetexthome) {
				rowView = inflater.inflate(layout, null);
				viewCache = new ViewCache(rowView);
				rowView.setTag(viewCache);
				
			} else {
				throw new RuntimeException("Unsupported layout");
			}

		}
		String sigla = getItem(position);
		String res = getItemResource(position);
		// carregar a imagem e inseri-la na imageview

		if (sigla != null) {

			ImageView imageView = viewCache.getImageView();
			imageView.setTag(sigla);

			Bitmap cachedImage = asyncImageLoader.loadDrawableFromResources(
					this.getContext(), sigla, res, new ImageCallback() {
						public void imageLoaded(Bitmap imageDrawable,
								final String imageTag) {
							final ImageView imageViewByTag = (ImageView) listView
									.findViewWithTag(imageTag);

							if (imageViewByTag != null) {
								imageViewByTag.setImageBitmap(imageDrawable);
							}

						}
					});
			if (cachedImage != null) {
				imageView.setImageBitmap(cachedImage);
			}
		}
		// Set the text on the TextView
		TextView textView = viewCache.getTextView();
		textView.setText(sigla);
		if (layout == R.layout.imagetext) {
			if (!ratingValue.containsKey(sigla)) {
				Channel c = db.getChannelBySigla(viewCache.getTextView()
						.getText().toString());
				int rate = c.isFavourite() ? 1 : 0;
				ratingValue.put(sigla, rate);
				viewCache.getRating().setRating(rate);
			} else {
				viewCache.getRating().setRating(ratingValue.get(sigla));
			}
		} else if (layout == R.layout.imagetextprograms) {
			if (!programRatinValue.containsKey(position)) {
				p.setId(programs.get(position).getId());
				p.setName(sigla);
				if (favouritePrograms != null && favouritePrograms.contains(p)) {
					programRatinValue.put(position, 1);
					viewCache.getRating().setRating(1);
				} else {
					programRatinValue.put(position, 0);
					viewCache.getRating().setRating(0);
				}

			} else {
				viewCache.getRating().setRating(programRatinValue.get(position));
			}
			TextView t = (TextView) rowView.findViewById(R.id.idTextViewHour);
			if (programHour != null)
				t.setText(programHour.get(position));
			else
				t.setText("n def.");
		}
		if(layout == R.layout.imagetextprograms)
			viewCache.getRating().setTag(position);
		else
			viewCache.getRating().setTag(sigla);
		return rowView;
	}

	private String getItemResource(int position) {
		if (imgresname != null)
			return imgresname.get(position);
		return getItem(position);
	}

}
