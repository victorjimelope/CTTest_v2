package dao.impl;

import static constants.Constants._EQUALS;
import static constants.Constants._FROM;
import static constants.Constants._LIKE;
import static constants.Constants._ORDER_BY;
import static constants.Constants._WHERE;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import dao.StudentGroupDao;
import model.StudentGroup;
import model.enums.noper.GenericSortType;
import model.noper.StringPair;
import util.ResUtil;

@Repository
public class StudentGroupDaoImpl extends GenericHibernateDaoImpl<StudentGroup, Exception>
	implements StudentGroupDao {
	
	private static final Logger logger = LogManager.getLogger(StudentGroupDaoImpl.class);
	
	
	public StudentGroupDaoImpl() {
		super();
		super.setEntityClass(StudentGroup.class);
	}
	
	
	@Override @SuppressWarnings("unchecked")
	public List<StudentGroup> finder(Integer centerId, String name,
			GenericSortType sortType) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			
			super.addFilter(mainQuery, "center.id", centerId, _EQUALS);
			
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
	public List<StringPair> getComboList(List<Integer> studentGroupIdList) throws Exception {
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					"SELECT new model.noper.StringPair(id, name) FROM "
							+ super.getEntityClass().getName());
			
			if (!CollectionUtils.isEmpty(studentGroupIdList)) {
				mainQuery.append(_WHERE + "id IN ("
						+ studentGroupIdList.stream()
								.map(String::valueOf)
								.collect(Collectors.joining(", "))
						+ ")");
			}
			
			return (List<StringPair>) getCurrentSession()
					.createQuery(mainQuery.toString())
					.getResultList();
			
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

	@Override @SuppressWarnings("unchecked")
	public List<StringPair> getComboList(Integer centerId) throws Exception {
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					"SELECT new model.noper.StringPair(id, name) FROM "
							+ super.getEntityClass().getName());
			
			super.addFilter(mainQuery, "center.id", centerId, _EQUALS);
			
			return (List<StringPair>) getCurrentSession()
					.createQuery(mainQuery.toString())
					.getResultList();
			
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
