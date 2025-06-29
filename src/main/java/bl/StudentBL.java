package bl;

import java.util.List;

import model.Student;
import model.enums.noper.GenericSortType;

public interface StudentBL extends GenericHibernateBL<Student, Exception> {
	
	public List<Student> finder(String name, Integer studentGroupId,
			GenericSortType sortType) throws Exception;
	
}