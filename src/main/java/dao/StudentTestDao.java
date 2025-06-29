package dao;

import java.math.BigDecimal;
import java.util.List;

import model.StudentTest;
import model.enums.StudentTestStatus;
import model.enums.noper.StudentTestSortType;

public interface StudentTestDao extends GenericHibernateDao<StudentTest, Exception> {

	public List<StudentTest> finder(Integer testInstanceId, String name,
			StudentTestStatus status, StudentTestSortType sortType) throws Exception;
	
	public StudentTest find(Integer testInstanceId, Integer studentIdentifier) throws Exception;
	
	public boolean isTestInstanceClosed(Integer id) throws Exception;
	
//	public StudentTestFull getStudentTestFullV2(Integer studentTestId);
	
	public void startTest(Integer id) throws Exception;
	
	public void finishTest(Integer id, StudentTestStatus newStatus,
			BigDecimal score) throws Exception;
	
}