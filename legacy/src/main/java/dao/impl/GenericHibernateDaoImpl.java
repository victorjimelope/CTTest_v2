package dao.impl;

import static constants.Constants._AND;
import static constants.Constants._FROM;
import static constants.Constants._WHERE;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import dao.GenericHibernateDao;
import lombok.Getter;
import lombok.Setter;
import util.ResUtil;

@Transactional
public class GenericHibernateDaoImpl<T extends Serializable, E extends Throwable> 
	implements GenericHibernateDao<T, E> {
	
	private static final Logger logger = LogManager.getLogger(GenericHibernateDaoImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Getter @Setter
	private Class<T> entityClass;
	
	protected Session getCurrentSession() {
	    return sessionFactory.getCurrentSession();
	}
	
	public void create(T entity) throws Exception {
    	
		try {
			
			getCurrentSession().save(entity);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_creating_xxx"),
					entityClass.getSimpleName()));
			
		}
		
    }
	
    public T read(int id) throws Exception {
    	
        try {
        	
        	T entity = getCurrentSession().find(entityClass, id);
            
            if (entity == null) {
            	throw new Exception(MessageFormat.format(
    					ResUtil.get("label_entity_xxx_with_id_yyy_not_found"),
    					entityClass.getSimpleName(), id));
            }
            
            return entity;
            
        } catch (Exception e) {
        	
        	StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_loading_xxx"),
					entityClass.getSimpleName()));
            
        }
        
    }
    
	public void update(final T entity) throws Exception {
    	
		try {
			
			getCurrentSession().saveOrUpdate(entity);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_updating_xxx"),
					entityClass.getSimpleName()));
			
		}

	}
	
	public void delete(int id) throws Exception {
    	
    	try {
			
    		T entity = read(id);
    		
			getCurrentSession().remove(entity);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_deleting_xxx"),
					entityClass.getSimpleName()));
			
		}
    	
    }
	
	@SuppressWarnings("unchecked")
	public List<T> findAll() throws Exception {
    	
    	 try {
         	
    		 String mainQuery = _FROM + entityClass.getSimpleName();
    		 
    		 return (List<T>) getCurrentSession()
    				 .createQuery(mainQuery).getResultList();
             
         } catch (Exception e) {
         	
        	StackTraceElement element = e.getStackTrace()[0];
 			
 			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
 					element.getClassName(), element.getMethodName(),
 					e.getClass().getName(), e.getMessage());
 			
 			e.printStackTrace();
 			
 			throw new Exception(MessageFormat.format(
 					ResUtil.get("label_error_loading_xxx"),
 					entityClass.getSimpleName()));
             
         }
    	 
    }
    
    public void close() {
    	if (sessionFactory != null) {
    		sessionFactory.close();
		}
    }
    
    public static void addFilter(StringBuilder query, String property,
    		String filter, String comparator) {
    	
        if (StringUtils.isBlank(query)
        		|| StringUtils.isBlank(property)
        		|| StringUtils.isBlank(filter)
        		|| StringUtils.isBlank(comparator)) {
        	return;
        }
        
        query.append(query.toString().contains(_WHERE) ? _AND : _WHERE)
        		.append(property).append(comparator).append(filter);
        
    }
    
    public static void addFilter(StringBuilder query, String property,
    		Integer filter, String comparator) {
    	
    	 if (StringUtils.isBlank(query)
         		|| StringUtils.isBlank(property)
         		|| filter == null
         		|| StringUtils.isBlank(comparator)) {
         	return;
         }
        
        query.append(query.toString().contains(_WHERE) ? _AND : _WHERE)
        		.append(property).append(comparator).append(filter);
        
    }
    
}