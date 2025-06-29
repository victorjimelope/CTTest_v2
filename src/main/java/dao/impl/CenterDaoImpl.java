package dao.impl;

import static constants.Constants._FROM;
import static constants.Constants._LIKE;
import static constants.Constants._ORDER_BY;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import dao.CenterDao;
import model.Center;
import model.enums.noper.GenericSortType;
import model.noper.StringPair;
import util.ResUtil;

@Repository
public class CenterDaoImpl extends GenericHibernateDaoImpl<Center, Exception>
	implements CenterDao {
	
	private static final Logger logger = LogManager.getLogger(GenericHibernateDaoImpl.class);
	
	
	public CenterDaoImpl() {
		super();
		super.setEntityClass(Center.class);
	}
	
	
	@Override @SuppressWarnings("unchecked")
	public List<Center> finder(String name, GenericSortType sortType) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			 
			if (!StringUtils.isBlank(name)) {
				super.addFilter(mainQuery, "name", "'%" + name + "%'", _LIKE);
			}
			
	        if (sortType != null) {
	        	mainQuery.append(_ORDER_BY + sortType.getSort());
	        }
	        
			return getCurrentSession()
					.createQuery(mainQuery.toString())
					.getResultList();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_loading_list_of_xxx"),
					super.getEntityClass().getSimpleName()), e);
			
		}
		
	}
	
	@Override @SuppressWarnings("unchecked")
	public List<StringPair> getComboList() throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			String mainQuery = "SELECT new model.noper.StringPair(id, name) FROM "
					+ super.getEntityClass().getName();
			
			return (List<StringPair>) getCurrentSession()
					.createQuery(mainQuery).getResultList();
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			e.printStackTrace();
			
			throw new Exception(MessageFormat.format(
					ResUtil.get("label_error_loading_combolist_of_xxx"),
					super.getEntityClass().getSimpleName()), e);
			
		}
		
	}
	
}
