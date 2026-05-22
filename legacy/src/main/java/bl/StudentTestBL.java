package bl;

import java.util.List;

import model.StudentTest;
import model.enums.StudentTestStatus;
import model.enums.noper.StudentTestSortType;

public interface StudentTestBL extends GenericHibernateBL<StudentTest, Exception> {
	
	public List<StudentTest> finder(Integer testInstanceId, String name,
			StudentTestStatus status, StudentTestSortType sortType) throws Exception;
	
	public boolean isTestInstanceClosed(Integer id) throws Exception;
	
	public StudentTest find(Integer testInstanceId, Integer studentIdentifier) throws Exception;
	
	public void startTest(Integer id) throws Exception;
	
	public void finishTest(Integer id) throws Exception;
	
}