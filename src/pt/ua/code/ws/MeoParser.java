package pt.ua.code.ws;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



public class MeoParser extends DefaultHandler{
	private final String xml;
	private String tagvalue;
	private Channel tmpChannel;
	private Program tmpProgram;
	private LinkedList<Channel> channels;
	private LinkedList<Program> programs;
	
	private SimpleDateFormat df=  new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
	private enum PARSEMODE {
		Channel, Program
	};
	PARSEMODE mode = null;
	
	public MeoParser(String xml) {
		this.xml=xml;
		
	}
	
	public LinkedList<Program> getProgramsFromXML(){
		programs = new LinkedList<Program>();
		mode=PARSEMODE.Program;
		startParse();
		return programs;
	}
	
	public LinkedList<Channel> getChannelFromXML(){
		channels = new LinkedList<Channel>();
		mode=PARSEMODE.Channel;
		startParse();
		return channels;
	}
	private void startParse(){
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			SAXParser sp = spf.newSAXParser();
			if(xml==null || xml.compareTo("")==0)
				return;
			sp.parse(new InputSource(new java.io.StringReader(xml)), this);

		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		tagvalue += new String(ch, start, length);
	}
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		tagvalue="";
		if (localName.equalsIgnoreCase("Program")) {
			tmpProgram=new Program();
			programs.add(tmpProgram);
		}else if (localName.equalsIgnoreCase("Channel")) {
			tmpChannel=new Channel();
			channels.add(tmpChannel);
		}
		
	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(mode==PARSEMODE.Channel){
			if (localName.equalsIgnoreCase("Name")&& (tmpChannel.getName()==null || tmpChannel.getName().compareTo("")==0)) {
				tmpChannel.setName(tagvalue);
			}else if (localName.equalsIgnoreCase("Sigla")) {
				tmpChannel.setSigla(tagvalue);
			}
		}else if(mode == PARSEMODE.Program){
			if (localName.equalsIgnoreCase("Id") && (tmpProgram.getId()==null || tmpProgram.getId().compareTo("")==0)) {
				tmpProgram.setId(tagvalue);
			}else if (localName.equalsIgnoreCase("Title")) {
				tmpProgram.setName(tagvalue);
			}else if (localName.equalsIgnoreCase("Description") && (tmpProgram.getDescription()==null || tmpProgram.getDescription().compareTo("")==0)) {
				tmpProgram.setDescription(tagvalue);
			}else if (localName.equalsIgnoreCase("StartTime")) {
				try {
					tmpProgram.setBegin( df.parse(tagvalue));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (localName.equalsIgnoreCase("EndTime")) {
				try {
					tmpProgram.setEnd(df.parse(tagvalue));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
