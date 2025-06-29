package bl;

import java.util.List;

import model.LoggedUser;
import model.User;
import model.enums.noper.GenericSortType;

public interface UserBL extends GenericHibernateBL<User, Exception> {
	
	public List<User> finder(Integer centerId, String name,
			GenericSortType sortType) throws Exception;
	
	/**
	 * Devuelve el usuario dado el nombre
	 * 
	 * @author victorjl 20250107
	 * 
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public User findByUserName(String username) throws Exception;

	public LoggedUser getLoggedUser(String username) throws Exception;
	
}