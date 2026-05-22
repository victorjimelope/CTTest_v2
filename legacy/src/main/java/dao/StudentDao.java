package dao;

import java.util.List;

import model.Student;
import model.enums.noper.GenericSortType;

public interface StudentDao extends GenericHibernateDao<Student, Exception> {

	public List<Student> finder(String name, Integer studentGroupId,
			GenericSortType sortType) throws Exception;
	
}