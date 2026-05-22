package dao;

import java.util.List;

import model.Test;
import model.enums.TestStatus;
import model.enums.TestType;
import model.enums.noper.GenericSortType;

public interface TestDao extends GenericHibernateDao<Test, Exception> {

	public List<Test> finder(Integer centerId, String name,
			TestStatus status, GenericSortType sortType) throws Exception;

	public List<Test> explore(Integer centerId, String name,
			GenericSortType sortType) throws Exception;
	
	public void changeStatus(Integer testId, TestStatus newStatus,
			Integer loggedUserId) throws Exception;
	
	public void changeType(Integer testId, TestType newType,
			Integer loggedUserId) throws Exception;
	
}