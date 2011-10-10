package pt.ua.code.favouritetv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import pt.ua.code.favouritetv.calendar.CalendarManager;
import pt.ua.code.favouritetv.facebook.BaseDialogListener;
import pt.ua.code.favouritetv.facebook.BaseRequestListener;
import pt.ua.code.favouritetv.facebook.LoginButton;
import pt.ua.code.favouritetv.facebook.SessionEvents;
import pt.ua.code.favouritetv.facebook.SessionEvents.AuthListener;
import pt.ua.code.favouritetv.facebook.SessionEvents.LogoutListener;
import pt.ua.code.favouritetv.facebook.SessionStore;
import pt.ua.code.favouritetv.gui.ImagemETextoListAdapter;
import pt.ua.code.ws.MeoWsClient;
import pt.ua.code.ws.Program;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.R;
import com.facebook.android.Util;

public class Programs extends Activity {
	// should save
	private HashMap<Integer, List<Program>> cacheprograms = new HashMap<Integer, List<Program>>();
	private Date start;
	private Date end = new Date();
	private int currentposition = 0;
	private String verprograma = "";
	private String sigla;
	
	//
	String[] days = {
			"", "Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sabado"};
	private DateFormatSymbols symbols;
	private SimpleDateFormat dfDay;
	private SimpleDateFormat dfDate;
	private ViewFlipper vflipper;
	private SimpleDateFormat df = new SimpleDateFormat("HH:mm");
	private ListView listview;
	private final List<String> listaprograms = new LinkedList<String>();
	private final List<String> listaresources = new LinkedList<String>();
	private final List<String> listaphour = new LinkedList<String>();
	private List<Program> programs;
	public static final String APP_ID = "125230214223020";
	// facebook
	private LoginButton mLoginButton;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private TextView txtDay , txtDate;
	private Dialog dialog;
	private Button next, prev;
	private MeoWsClient wsc;
	private int selectedprogram;
	private CalendarManager cm;
	private Dialog dialogfb;
	private Dialog facebook;
	private Dialog programInfo;
	private TextView txtInfoName, txtInfoStart, txtInfoEnd, txtInfoDescription;
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			String mensagem = ((String)msg.obj);
			if(mensagem!=null && mensagem.compareToIgnoreCase("NETWORK_ERROR")==0){
				Toast.makeText(Programs.this, "Houve um problema com a rede!",
						Toast.LENGTH_LONG).show();
			}else{
				updateList();
			}
		}
	};
	AsyncProgramGetter pgetter = new AsyncProgramGetter();

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		if(v==listview){
			selectedprogram=info.position;
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.contextmenu, menu);
			menu.setHeaderTitle(programs.get(selectedprogram).getName());
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(selectedprogram>=0){
			programInfo.setTitle("Info: "+programs.get(selectedprogram).getName());
			txtInfoName.setText(programs.get(selectedprogram).getName());
			txtInfoDescription.setText(programs.get(selectedprogram).getDescription());
			txtInfoStart.setText(df.format(programs.get(selectedprogram).getBegin()));
			txtInfoEnd.setText(df.format(programs.get(selectedprogram).getEnd()));
			programInfo.show();
		}
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		outState.putSerializable("cacheprograms", cacheprograms);
		outState.putSerializable("start", start);
		outState.putSerializable("end", end);
		outState.putInt("currentposition", currentposition);
		outState.putString("verprograma", verprograma);
		outState.putString("psigla", sigla);

		super.onSaveInstanceState(outState);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.programs);
		symbols = new DateFormatSymbols(new Locale("pt", "BR"));
		symbols.setShortWeekdays(days);
		dfDay = new SimpleDateFormat("E", symbols);
		dfDate = new SimpleDateFormat("dd-MM-yy", symbols);
		
		dialog = new ProgressDialog(this);
		next = (Button) this.findViewById(R.id.next_program);
		prev = (Button) this.findViewById(R.id.prev_program);
		next.setEnabled(false);
		prev.setEnabled(false);
		
		
		wsc = new MeoWsClient(this);
		start = Calendar.getInstance().getTime();
		listview = (ListView) findViewById(R.id.listPrograms);
		cm = new CalendarManager(this);
		
		if (savedInstanceState==null || !savedInstanceState.containsKey("psigla")) {
			sigla = getIntent().getStringExtra("sigla");
			dialog.setTitle("Obtendo os programas da " + sigla);
			dialog.show();
			
		}else{
			cacheprograms = (HashMap<Integer, List<Program>>) savedInstanceState.getSerializable("cacheprograms");
			start = (Date) savedInstanceState.getSerializable("start");
			end = (Date) savedInstanceState.getSerializable("end");
			currentposition = savedInstanceState.getInt("currentposition");
			verprograma= savedInstanceState.getString("verprograma");
			sigla = savedInstanceState.getString("psigla");
		}
		
		dialogfb = new Dialog(this);
		facebook = new Dialog(this);
		facebook.setContentView(R.layout.facebook);
		facebook.setTitle("Facebook");
		mFacebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);

		SessionStore.restore(mFacebook, this);
		SessionEvents.addAuthListener(new SampleAuthListener());
		SessionEvents.addLogoutListener(new SampleLogoutListener());
		mLoginButton = (LoginButton) facebook.findViewById(R.id.login);

		mLoginButton.init(this, mFacebook,
				new String[] { "publish_stream" });

		dialogfb.setContentView(R.layout.dialog);
		dialogfb.setTitle("Facebook");
		Button btnOk = (Button) dialogfb.findViewById(R.id.btnOk);
		Button btnCancel = (Button) dialogfb.findViewById(R.id.btnCancel);
		btnOk.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (APP_ID == null) {
					Util.showAlert(
							Programs.this,
							"Warning",
							"Facebook Applicaton ID must be "
									+ "specified before running this example: see Example.java");

				}
				dialogfb.dismiss();

				if (mFacebook.isSessionValid()) {

					Bundle params = new Bundle();
					params.putString("message", "Vou ver " + verprograma);
					try {
						mFacebook.request("me/feed/", params, "POST");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast.makeText(Programs.this,
							"Post adicionado ao mural!", Toast.LENGTH_LONG)
							.show();
					return;
				}
				facebook.show();

			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				dialogfb.dismiss();

			}
		});

		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentposition++;
				if (!cacheprograms.containsKey(currentposition))
					dialog.show();

				pgetter = new AsyncProgramGetter();
				pgetter.start();
			}
		});
		prev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentposition--;
				if (!cacheprograms.containsKey(currentposition))
					dialog.show();
				pgetter = new AsyncProgramGetter();
				pgetter.setGetMode(GETMODE.PREV);
				pgetter.start();
			}
		});
		programInfo= new Dialog(this);
		programInfo.setContentView(R.layout.information);
		txtInfoName = (TextView)programInfo.findViewById(R.id.txtInfoName);
		txtInfoStart = (TextView)programInfo.findViewById(R.id.txtInfoStart);
		txtInfoEnd = (TextView)programInfo.findViewById(R.id.txtInfoEnd);
		txtInfoDescription = (TextView)programInfo.findViewById(R.id.txtInfoDescription);
		
		vflipper = (ViewFlipper) findViewById(R.id.program_flipper_date);
		vflipper.startFlipping();
		vflipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		vflipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		txtDay = (TextView)vflipper.findViewById(R.id.idTextViewDay);
		txtDate = (TextView)vflipper.findViewById(R.id.idTextViewDate);
		txtDay.setText(dfDay.format(start));
		txtDate.setText(dfDate.format(start));
		registerForContextMenu(listview);
		//start the thread that fills the list
		pgetter.start();
		
	}

	private void updateList() {
		listview.setAdapter(new ImagemETextoListAdapter(this, listaprograms,
				listview, R.layout.imagetextprograms, listaresources,
				listaphour, programs));
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				RatingBar r = (RatingBar) arg1
						.findViewById(R.id.ratingfavourite);
				Integer rate = ((ImagemETextoListAdapter) listview.getAdapter())
						.getRatingValue((Integer) r.getTag());
				HashMap<Integer, String> activecalendars = cm
						.getActiveCalendars();
				if (rate == 0) {

					if (activecalendars == null || activecalendars.isEmpty()) {
						Toast.makeText(
								Programs.this,
								"Não é possível adicionar programas ao calendário! Nenhum calendário está disponível!",
								Toast.LENGTH_LONG).show();
						return;
					}
					Program p = programs.get(arg2);
					cm.addProgram(p, activecalendars.keySet().iterator().next());
					r.setRating(1);
					((ImagemETextoListAdapter) listview.getAdapter())
							.setRatingValue((Integer) r.getTag(), 1);
					verprograma = p.getName();
					dialogfb.show();

				} else {

					if (activecalendars == null || activecalendars.isEmpty()) {
						Toast.makeText(
								Programs.this,
								"Não é possível adicionar programas ao calendário! Nenhum calendário está disponível!",
								Toast.LENGTH_LONG).show();
						return;
					}
					cm.removeProgram(programs.get(arg2), activecalendars
							.keySet().iterator().next());
					r.setRating(0);
					((ImagemETextoListAdapter) listview.getAdapter())
							.setRatingValue((Integer) r.getTag(), 0);
				}
				
				FavouriteTV.getContextAlerterService().refreshPrograms();
			}
		});
		next.setEnabled(currentposition < 7);
		prev.setEnabled(currentposition > 0);
		txtDay.setText(dfDay.format(start));
		txtDate.setText(dfDate.format(start));
	}

	private enum GETMODE {
		PREV, NEXT
	}

	private class AsyncProgramGetter extends Thread {
		GETMODE mode = GETMODE.NEXT;

		public void run() {

			if (mode == GETMODE.NEXT) {
				if (currentposition != 0) {
					start.setTime(end.getTime());
					start.setHours(24);
					start.setMinutes(0);
					start.setSeconds(0);
					end.setTime(start.getTime());
					end.setHours(23);
					end.setMinutes(59);
					end.setSeconds(59);
				} else {
					end.setTime(start.getTime());
					end.setHours(23);
					end.setMinutes(59);
					end.setSeconds(59);
				}

			} else {
				if (currentposition != 0) {
					end.setTime(start.getTime());
					end.setDate(end.getDate() - 1);
					end.setHours(0);
					end.setMinutes(0);
					end.setSeconds(0);

					start.setTime(end.getTime());
					start.setHours(23);
					start.setMinutes(59);
					start.setSeconds(59);
				} else {
					start = Calendar.getInstance().getTime();
					end.setTime(start.getTime());
					end.setHours(23);
					end.setMinutes(59);
					end.setSeconds(59);
				}

			}

			if (cacheprograms.containsKey(currentposition)) {
				programs = cacheprograms.get(currentposition);
			} else {
				programs = wsc.getProgramsByDateRest(start, end, sigla,handler);
				cacheprograms.put(currentposition, programs);
			}
			if (programs != null) {
				listaphour.clear();
				listaprograms.clear();
				listaresources.clear();

				for (Program p : programs) {
					listaprograms.add(p.getName());
					listaresources.add("tv");

					if (p.getBegin() != null)
						listaphour.add(df.format(p.getBegin()));
					else
						listaphour.add(null);
				}
			}
			// call handler
			handler.sendMessage(handler.obtainMessage());
			if (dialog.isShowing())
				dialog.dismiss();
			
		}

		void setGetMode(GETMODE mode) {
			this.mode = mode;
		}
	}

	public class SampleAuthListener implements AuthListener {

		public void onAuthSucceed() {

			Bundle params = new Bundle();
			params.putString("message", "Vou ver " + verprograma);
			try {
				mFacebook.request("me/feed/", params, "POST");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Toast.makeText(Programs.this, "Post adicionado ao mural!",
					Toast.LENGTH_LONG).show();
			return;

		}

		public void onAuthFail(String error) {
			Toast.makeText(Programs.this, "Erro no login: " + error,
					Toast.LENGTH_LONG).show();
		}
	}

	public class SampleLogoutListener implements LogoutListener {
		public void onLogoutBegin() {
			Toast.makeText(Programs.this, "Logout ...", Toast.LENGTH_LONG)
					.show();
		}

		public void onLogoutFinish() {
			Toast.makeText(Programs.this, "Terminaste a sessão",
					Toast.LENGTH_LONG).show();

		}
	}

	public class SampleDialogListener extends BaseDialogListener {

		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				Log.d("Facebook", "Dialog Success! post_id=" + postId);
				mAsyncRunner.request(postId, new WallPostRequestListener());

			} else {
				Log.d("Facebook", "No wall post made");
			}
		}
	}

	public class WallPostRequestListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			Log.d("Facebook-Example", "Got response: " + response);
			String message = "<empty>";
			try {
				JSONObject json = Util.parseJson(response);
				message = json.getString("message");
			} catch (JSONException e) {
				Log.w("Facebook-Example", "JSON Error in response");
			} catch (FacebookError e) {
				Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
			}
			final String text = "Your Wall Post: " + message;
			Programs.this.runOnUiThread(new Runnable() {
				public void run() {

					Toast.makeText(Programs.this, text, Toast.LENGTH_LONG)
							.show();
				}
			});
		}
	}

}
