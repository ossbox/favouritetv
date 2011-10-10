package pt.ua.code.ws;

import java.io.Serializable;
import java.util.Date;


public class Program implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3456364720374983636L;

	private String name;
	private String description;
	private String id;
	private Date begin;
	private Date end;
	private boolean isFavourite;
	
	public Program() {
		// TODO Auto-generated constructor stub
	}
	public Program(String name, String description, String id) {
		// TODO Auto-generated constructor stub
		this.id=id;
		this.name=name;
		this.description = description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Date getBegin() {
		return begin;
	}
	public Date getEnd() {
		return end;
	}
	public boolean isFavourite() {
		return isFavourite;
	}
	public void setFavourite(boolean isFavourite) {
		this.isFavourite = isFavourite;
	}
	public void setBegin(Date begin) {
		this.begin = begin;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Program other = (Program) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
	
}
