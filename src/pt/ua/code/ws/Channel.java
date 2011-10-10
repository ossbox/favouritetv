package pt.ua.code.ws;

import java.io.Serializable;

public class Channel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1736714874874603721L;
	private String name;
	private String sigla;
	private boolean isFavourite=false;
	public Channel(String name,  String sigla) {
		// TODO Auto-generated constructor stub
		this.name=name;
		this.sigla=sigla;
	}
	public Channel(String name,  String sigla, boolean favourite) {
		// TODO Auto-generated constructor stub
		this.name=name;
		this.sigla=sigla;
		this.isFavourite = favourite;
	}
	
	public Channel() {
		// TODO Auto-generated constructor stub
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setSigla(String sigla) {
		this.sigla = sigla;
	}
	public String getSigla() {
		return sigla;
	}
	public String getName() {
		return name;
	}
	public boolean isFavourite() {
		return isFavourite;
	}
	public void setFavourite(boolean isFavourite) {
		this.isFavourite = isFavourite;
	}
}
