package bl;

import java.util.List;

import model.Center;
import model.enums.noper.GenericSortType;
import model.noper.StringPair;

public interface CenterBL extends GenericHibernateBL<Center, Exception> {
	
	public List<Center> finder(String name, GenericSortType sortType) throws Exception;
	
	public List<StringPair> getComboList() throws Exception;
	
}