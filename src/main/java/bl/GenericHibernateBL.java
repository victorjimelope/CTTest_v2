package bl;

import java.io.Serializable;
import java.util.List;

public interface GenericHibernateBL<T extends Serializable, E extends Throwable> {

	void create(T entity, Integer loggedUserId) throws Exception;
	
	T read(int id) throws Exception;
	
	void update(T entity, Integer loggedUserId) throws Exception;
	
	void delete(int id) throws Exception;
	
	List<T> findAll() throws Exception;
	
}