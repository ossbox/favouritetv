package pt.ua.code.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class MeoWsClient {

	private String serviceURL = "213.13.145.106";// "services.sapo.pt";
	private int port = 80;
	private Context context;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public MeoWsClient(Context context) {
		this.context = context;
	}

	public MeoWsClient(Context context, String serviceURL) {
		this.serviceURL = serviceURL;
		this.context = context;

	}

	public MeoWsClient(Context context, String serviceURL, int port) {
		this.serviceURL = serviceURL;
		this.port = port;
		this.context = context;
	}

	public LinkedList<Channel> getChannelsRest() {

		LinkedList<Channel> channels = null;
		// GetChannelList

		String response = callRestService("/EPG/GetChannelList?");
		channels = new MeoParser(response).getChannelFromXML();

		return channels;
	}

	public LinkedList<Program> getProgramsByDateRest(Date start, Date end,
			String sigla) {

		LinkedList<Program> programs = null;
		// GetProgramListByChannelDateInterval
		String sStart = df.format(start).toString().replace(" ", "%20");
		;
		String sEnd = df.format(end).toString().replace(" ", "%20");
		sigla = sigla.replace(" ", "%20");
		String response = callRestService("/EPG/GetProgramListByChannelDateInterval?channelSigla="
				+ sigla + "&startDate=" + sStart + "&endDate=" + sEnd);
		programs = new MeoParser(response).getProgramsFromXML();

		return programs;
	}

	private String callRestService(String service) {
		String response = null;
		try {
			response = _callRestService(service);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Toast.makeText(context, "Houve um problema no serviço!",
					Toast.LENGTH_LONG).show();

		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, "Houve um problema com a rede!",
					Toast.LENGTH_LONG).show();
		}
		return response;
	}

	private String _callRestService(String service) throws IOException {
		String response = null;

		URLConnection connection = new URL("http", serviceURL, port, service)
				.openConnection();
		InputStream is = connection.getInputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buf = new byte[512];
		int rd;
		while ((rd = is.read(buf, 0, 512)) != -1) {
			bos.write(buf, 0, rd);
		}
		bos.flush();
		response = new String(bos.toByteArray());
		return response;
	}

	public List<Program> getProgramsByDateRest(Date start, Date end,
			String sigla, Handler handler) {

		LinkedList<Program> programs = null;
		// GetProgramListByChannelDateInterval
		String sStart = df.format(start).toString().replace(" ", "%20");
		;
		String sEnd = df.format(end).toString().replace(" ", "%20");
		sigla = sigla.replace(" ", "%20");
		String response=null;
		try {
			response = _callRestService("/EPG/GetProgramListByChannelDateInterval?channelSigla="
					+ sigla + "&startDate=" + sStart + "&endDate=" + sEnd);
			
		} catch (IOException e) {
			Message msg = handler.obtainMessage();
			msg.obj = "NETWORK_ERROR";
			handler.sendMessage(msg);
			e.printStackTrace();
		}
		programs = new MeoParser(response).getProgramsFromXML();
		return programs;
	}

}
