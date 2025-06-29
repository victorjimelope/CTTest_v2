package bl;

import java.util.List;

import model.TestInstance;

public interface TestInstanceBL extends GenericHibernateBL<TestInstance, Exception> {

	public void createTestInstance(TestInstance entity, Integer loggedUserId) throws Exception;
	
	public List<TestInstance> finder(Integer testId) throws Exception;
	
	public Integer getIdByPin(Integer pin) throws Exception;

	public Integer getTestId(Integer id) throws Exception;
	
	public void closeTestInstance(Integer id, Integer loggedUserId) throws Exception;
	
}