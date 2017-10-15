/**
 * 
 */
package com.unibet.worktest.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Supporting class to provide beans from application context 
 * and initialize the context if it is already not created.
 * 
 * @author Moupiya
 *
 */
public class ContextProvider {
	
	private static ApplicationContext context;
	
	private static void initializeContext() {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName) {
		if (context == null) {
			synchronized(ContextProvider.class)
			{
				if (context == null) {
					initializeContext();
				}
			}
		}
		return (T) context.getBean(beanName);
	}

}
