package it.thecrawlers.crawler.dao;

import it.thecrawlers.model.Review;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReviewDAOImpl implements ReviewDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public <S extends Review> S save(S entity) {
		sessionFactory.getCurrentSession().persist(entity);
		return entity;
	}

	@Override
	public <S extends Review> Iterable<S> save(Iterable<S> entities) {
		sessionFactory.getCurrentSession().persist(entities);
		return entities;
	}

	@Override
	public Review findOne(String id) {
		return (Review) sessionFactory.getCurrentSession().get(Review.class, id);
	}

	@Override
	public boolean exists(String id) {
		return (findOne(id) == null ? false : true);
	}

	@Override
	public Iterable<Review> findAll() {
		@SuppressWarnings("unchecked")
		List<Review> ReviewList = sessionFactory.getCurrentSession().createQuery("from Review").list();
		return ReviewList;
	}

	@Override
	public Iterable<Review> findAll(Iterable<String> ids) {
		String idList = StringUtils.join(ids, ",");
		@SuppressWarnings("unchecked")
		List<Review> ReviewList = sessionFactory.getCurrentSession().createQuery("from Review where id in (:idList)")
				.setString("idList", idList).list();
		return ReviewList;
	}

	@Override
	public long count() {
		@SuppressWarnings("unchecked")
		List<Review> ReviewList = sessionFactory.getCurrentSession().createQuery("from Review").list();
		return ReviewList.size();
	}

	@Override
	public void delete(String id) {
		sessionFactory.getCurrentSession().createQuery("delete from Review where id = :id").setString("id", id)
				.executeUpdate();
	}

	@Override
	public void delete(Review entity) {
		sessionFactory.getCurrentSession().delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Review> entities) {
		sessionFactory.getCurrentSession().delete(entities);
	}

	@Override
	public void deleteAll() {
		sessionFactory.getCurrentSession().createQuery("delete from Review").executeUpdate();
	}

}