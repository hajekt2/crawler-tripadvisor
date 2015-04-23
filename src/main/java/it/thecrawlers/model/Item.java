package it.thecrawlers.model;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="Item")
public class Item {

	@Id
	@Column(name="itemId")
	private String id;
	
	@Column(name="name")
	private String name;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinTable(name="ItemReviews",
		joinColumns=@JoinColumn(name="itemId"),
		inverseJoinColumns=@JoinColumn(name="reviewId")
	)
	private Set<Review> reviews;
	
	@Column(name="crawlDate")
	private Date crawlDate;

	@Column(name="locationId")
	private String locationId;	

	@Column(name="url")
	private String url;	
	
	public Item(){
		super();
		reviews = new HashSet<Review>();
		crawlDate = new Date(System.currentTimeMillis());
	}
	
	public Item(String id, String name, Set<Review> reviews, Date crawlDate, String locationId, String url) {
		this();
		this.id = id;
		this.name = name;
		this.reviews = reviews;
		this.crawlDate = crawlDate;
		this.locationId = locationId;
		this.url = url;
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

	public Set<Review> getReviews() {
		return reviews;
	}

	public void setReviews(Set<Review> reviews) {
		this.reviews = reviews;
	}

	public Date getCrawlDate() {
		return crawlDate;
	}

	public void setCrawlDate(Date crawlDate) {
		this.crawlDate = crawlDate;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Item [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", reviews=");
		builder.append(reviews);
		builder.append(", crawlDate=");
		builder.append(crawlDate);
		builder.append(", locationId=");
		builder.append(locationId);
		builder.append(", url=");
		builder.append(url);
		builder.append("]");
		return builder.toString();
	}
	
}