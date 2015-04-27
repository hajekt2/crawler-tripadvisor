package it.thecrawlers.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Review")
public class Review {

	@Id
	@Column(name = "reviewId")
	private String id;

	@Column(name = "date")
	private Date date;

	@Column(name = "author")
	private String author;

	@Column(name = "rating")
	private int rating;

	@Column(name = "title")
	private String title;

	@Column(name = "text", columnDefinition = "TEXT")
	private String text;

	@Column(name="crawlDate")
	private Date crawlDate;

	@ManyToOne(fetch = FetchType.EAGER)
	private Item item;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
	
	public Date getCrawlDate() {
		return crawlDate;
	}

	public void setCrawlDate(Date crawlDate) {
		this.crawlDate = crawlDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Review [id=");
		builder.append(id);
		builder.append(", date=");
		builder.append(date);
		builder.append(", author=");
		builder.append(author);
		builder.append(", rating=");
		builder.append(rating);
		builder.append(", title=");
		builder.append(title);
		builder.append(", text=");
		builder.append(text);
		builder.append(", crawlDate=");
		builder.append(crawlDate);
		builder.append(", item=");
		builder.append(item);
		builder.append("]");
		return builder.toString();
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
		Review other = (Review) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}