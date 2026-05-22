package dao;

import java.util.List;

import model.TestInstance;

public interface TestInstanceDao extends GenericHibernateDao<TestInstance, Exception> {

	public List<TestInstance> finder(Integer testId) throws Exception;
	
	public boolean isPinAvailable(Integer pin) throws Exception;

	public Integer getIdByPin(Integer pin) throws Exception;

	public Integer getTestId(Integer id) throws Exception;
	
}