package it.thecrawlers.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
	private String rating;

	@Column(name = "title")
	private String title;

	@Column(name = "text")
	private String text;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "itemId")
	private Item item;
	
	public Review(String id, Date date, String author, String rating, String title, String text, Item item) {
		super();
		this.id = id;
		this.date = date;
		this.author = author;
		this.rating = rating;
		this.title = title;
		this.text = text;
		this.item = item;
	}

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

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
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
	
}