package bl.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bl.LoginBL;
import bl.UserBL;
import exception.ValidationException;
import model.User;
import util.PasswordUtil;
import util.ResUtil;

@Service
public class LoginBLImpl implements LoginBL {
	
	@Autowired
	private UserBL serviceUserBL;
	
	@Override
	public boolean validateUser(String username, String password) throws Exception {
		
		_checkNullability(username, password);
		
        User user = serviceUserBL.findByUserName(username);
        
        return user != null && PasswordUtil.matches(password, user.getPassword());
        
    }
	
	private void _checkNullability(String username, String password) throws Exception {
		
		if (StringUtils.isBlank(username)) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_username")));
		}
		
		if (StringUtils.isBlank(password)) {
			throw new ValidationException(MessageFormat.format(
					ResUtil.get("label_field_xxx_is_required"),
					ResUtil.get("label_password")));
		}
		
	}
	
}
