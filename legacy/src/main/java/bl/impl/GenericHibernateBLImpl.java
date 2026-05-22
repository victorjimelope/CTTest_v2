package bl.impl;

import java.io.Serializable;
import java.util.List;

import bl.GenericHibernateBL;
import dao.GenericHibernateDao;

public abstract class GenericHibernateBLImpl<T extends Serializable, E extends Throwable> 
	implements GenericHibernateBL<T, E> {
	
	protected abstract GenericHibernateDao<T, E> getDao();
	
	
	public T read(int id) throws Exception {
		return getDao().read(id);
	}

	public void create(T entity, Integer loggedUserId) throws Exception {
		getDao().create(entity);
	}
	
	public void update(T entity, Integer loggedUserId) throws Exception {
		getDao().update(entity);
	}
	
	public void delete(int id) throws Exception {
		getDao().delete(id);
	}
	
	public List<T> findAll() throws Exception {
		return getDao().findAll();
	}
	
}