package it.thecrawlers.crawler.dao;

import java.io.Serializable;

import org.springframework.data.repository.CrudRepository;

public interface BaseRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {
}