package util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextUtil<T> implements ApplicationContextAware {
	
	private static ApplicationContext context;

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		SpringContextUtil.context = context;
	}
	
	public T initializeBean(Class<T> classReference) {
	    return context.getBean(classReference);
	}
	
}
