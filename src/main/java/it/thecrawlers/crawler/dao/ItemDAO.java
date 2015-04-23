package it.thecrawlers.crawler.dao;

import it.thecrawlers.model.Item;

import java.util.List;

public interface ItemDAO {
	public void save(Item item);

	public List<Item> list();

}