package it.thecrawlers.crawler.dao;

import it.thecrawlers.model.Item;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ItemDAOImpl implements ItemDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public <S extends Item> S save(S entity) {
		sessionFactory.getCurrentSession().persist(entity);
		return entity;
	}

	@Override
	public <S extends Item> Iterable<S> save(Iterable<S> entities) {
		sessionFactory.getCurrentSession().persist(entities);
		return entities;
	}

	@Override
	public Item findOne(String id) {
		return (Item) sessionFactory.getCurrentSession().get(Item.class, id);
	}

	@Override
	public boolean exists(String id) {
		return (findOne(id) == null ? false : true);
	}

	@Override
	public Iterable<Item> findAll() {
		@SuppressWarnings("unchecked")
		List<Item> ItemList = sessionFactory.getCurrentSession().createQuery("from Item").list();
		return ItemList;
	}

	@Override
	public Iterable<Item> findAll(Iterable<String> ids) {
		String idList = StringUtils.join(ids, ",");
		@SuppressWarnings("unchecked")
		List<Item> ItemList = sessionFactory.getCurrentSession().createQuery("from Item where id in (:idList)")
				.setString("idList", idList).list();
		return ItemList;
	}

	@Override
	public long count() {
		@SuppressWarnings("unchecked")
		List<Item> ItemList = sessionFactory.getCurrentSession().createQuery("from Item").list();
		return ItemList.size();
	}

	@Override
	public void delete(String id) {
		sessionFactory.getCurrentSession().createQuery("delete from Item where id = :id").setString("id", id)
				.executeUpdate();
	}

	@Override
	public void delete(Item entity) {
		sessionFactory.getCurrentSession().delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Item> entities) {
		sessionFactory.getCurrentSession().delete(entities);
	}

	@Override
	public void deleteAll() {
		sessionFactory.getCurrentSession().createQuery("delete from Item").executeUpdate();
	}

}