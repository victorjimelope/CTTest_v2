package dao;

import java.io.Serializable;
import java.util.List;

public interface GenericHibernateDao<T extends Serializable, E extends Throwable> {

	void create(T entity) throws Exception;
	
	T read(int id) throws Exception;
	
	void update(T entity) throws Exception;
	
	void delete(int id) throws Exception;
	
	List<T> findAll() throws Exception;
	
}