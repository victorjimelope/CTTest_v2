package bl;

import java.util.List;

import model.StudentGroup;
import model.enums.noper.GenericSortType;
import model.noper.StringPair;

public interface StudentGroupBL extends GenericHibernateBL<StudentGroup, Exception> {

	public List<StudentGroup> finder(Integer centerId, String name,
			GenericSortType sortType) throws Exception;
	
	public List<StringPair> getComboList(List<Integer> studentGroupIdList) throws Exception;
	
	public List<StringPair> getComboList(Integer centerId) throws Exception;
	
}