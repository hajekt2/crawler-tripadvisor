package it.thecrawlers.model;


import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="Location")
public class Location {

	@Id
	@Column(name="locationId")
	private String id;
	
	@Column(name="name")
	private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parentId")
    private Location parent;
	
	@Column(name="crawlDate")
	private Date crawlDate;

	public Location(){
		super();
		crawlDate = new Date(System.currentTimeMillis());
	}
	
	public Location(String id, String name, Location parent) {
		this();
		this.id = id;
		this.name = name;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCrawlDate() {
		return crawlDate;
	}

	public void setCrawlDate(Date crawlDate) {
		this.crawlDate = crawlDate;
	}

	public Location getParent() {
		return parent;
	}

	public void setParent(Location parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Location other = (Location) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Location [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", parent=");
		builder.append(parent);
		builder.append(", crawlDate=");
		builder.append(crawlDate);
		builder.append("]");
		return builder.toString();
	}
	
}