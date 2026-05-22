package dao;

import java.util.List;

import model.Center;
import model.enums.noper.GenericSortType;
import model.noper.StringPair;

public interface CenterDao extends GenericHibernateDao<Center, Exception> {

	public List<Center> finder(String name, GenericSortType sortType) throws Exception;

	public List<StringPair> getComboList() throws Exception;
	
}