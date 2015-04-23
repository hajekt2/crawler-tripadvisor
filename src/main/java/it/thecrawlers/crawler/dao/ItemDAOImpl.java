package it.thecrawlers.crawler.dao;

import it.thecrawlers.model.Item;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ItemDAOImpl implements ItemDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	@Transactional
	public void save(Item p) {
		sessionFactory.getCurrentSession().persist(p);
	}

	@Override
	@Transactional
	public List<Item> list() {
		@SuppressWarnings("unchecked")
		List<Item> ItemList = sessionFactory.getCurrentSession().createQuery("from Item").list();
		return ItemList;
	}

}