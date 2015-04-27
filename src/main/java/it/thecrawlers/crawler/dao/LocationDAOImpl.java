package it.thecrawlers.crawler.dao;

import it.thecrawlers.model.Location;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class LocationDAOImpl implements LocationDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public <S extends Location> S save(S entity) {
		sessionFactory.getCurrentSession().persist(entity);
		return entity;
	}

	@Override
	public <S extends Location> Iterable<S> save(Iterable<S> entities) {
		sessionFactory.getCurrentSession().persist(entities);
		return entities;
	}

	@Override
	public Location findOne(String id) {
		return (Location) sessionFactory.getCurrentSession().get(Location.class, id);
	}

	@Override
	public boolean exists(String id) {
		return (findOne(id) == null ? false : true);
	}

	@Override
	public Iterable<Location> findAll() {
		@SuppressWarnings("unchecked")
		List<Location> LocationList = sessionFactory.getCurrentSession().createQuery("from Location").list();
		return LocationList;
	}

	@Override
	public Iterable<Location> findAll(Iterable<String> ids) {
		String idList = StringUtils.join(ids, ",");
		@SuppressWarnings("unchecked")
		List<Location> LocationList = sessionFactory.getCurrentSession().createQuery("from Location where id in (:idList)")
				.setString("idList", idList).list();
		return LocationList;
	}

	@Override
	public long count() {
		@SuppressWarnings("unchecked")
		List<Location> LocationList = sessionFactory.getCurrentSession().createQuery("from Location").list();
		return LocationList.size();
	}

	@Override
	public void delete(String id) {
		sessionFactory.getCurrentSession().createQuery("delete from Location where id = :id").setString("id", id)
				.executeUpdate();
	}

	@Override
	public void delete(Location entity) {
		sessionFactory.getCurrentSession().delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Location> entities) {
		sessionFactory.getCurrentSession().delete(entities);
	}

	@Override
	public void deleteAll() {
		sessionFactory.getCurrentSession().createQuery("delete from Location").executeUpdate();
	}

}