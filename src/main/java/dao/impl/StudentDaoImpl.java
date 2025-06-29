package dao.impl;

import static constants.Constants._EQUALS;
import static constants.Constants._FROM;
import static constants.Constants._LIKE;
import static constants.Constants._ORDER_BY;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import dao.StudentDao;
import model.Student;
import model.enums.noper.GenericSortType;
import util.ResUtil;

@Repository
public class StudentDaoImpl extends GenericHibernateDaoImpl<Student, Exception>
	implements StudentDao {
	
	private static final Logger logger = LogManager.getLogger(StudentDaoImpl.class);
	
	public StudentDaoImpl() {
		super();
		super.setEntityClass(Student.class);
	}
	
	
	@Override @SuppressWarnings("unchecked")
	public List<Student> finder(String name, Integer studentGroupId,
			GenericSortType sortType) throws Exception {
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			
			StringBuilder mainQuery = new StringBuilder(
					_FROM + super.getEntityClass().getName());
			 
			if (!StringUtils.isBlank(name)) {
				super.addFilter(mainQuery, "name", "'%" + name + "%'", _LIKE);
			}
			
        	super.addFilter(mainQuery, "studentGroup.id", studentGroupId, _EQUALS);
        	
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
	
}
