package bl;

public interface LoginBL {
	
	/**
	 * Autentifica el usuario y contraseña
	 * 
	 * @author victorjl 20250106
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public boolean validateUser(String username, String password) throws Exception;
	
}