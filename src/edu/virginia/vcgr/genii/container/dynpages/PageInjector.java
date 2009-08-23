package edu.virginia.vcgr.genii.container.dynpages;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.servlet.http.Cookie;

import com.sun.corba.se.pept.transport.Connection;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class PageInjector
{
	static private void doInjection(Object obj, Field field, Object value) 
		throws IllegalArgumentException, IllegalAccessException
	{
		field.setAccessible(true);
		field.set(obj, value);
		field.setAccessible(false);
	}
	
	static private void injectCookie(Object page, Field field,
		Cookie []cookies, String cookieName) throws IOException, 
			IllegalArgumentException, IllegalAccessException
	{
		Cookie c = null;
		
		if (!field.getType().isAssignableFrom(Cookie.class))
			throw new IOException(String.format(
				"Cannot inject cookie into field %s -- wrong type!", 
				field.getName()));
		
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				if (cookie.getName().equals(cookieName))
				{
					c = cookie;
					break;
				}
			}
		}
		
		doInjection(page, field, c);
	}
	
	static private void injectParameter(Object page, Field field,
		String []values) throws IOException, IllegalArgumentException, IllegalAccessException
	{
		if (field.getType().equals(String[].class))
			doInjection(page, field, values);
		else if (field.getType().equals(String.class))
		{
			if (values == null || values.length == 0)
				doInjection(page, field, null);
			else
				doInjection(page, field, values[0]);
		} else
			throw new IOException(String.format(
				"Cannot inject parameter into field %s -- wront type!",
				field.getName()));
	}
	
	static private void injectObject(Object page, Field field,
		InjectionContext context) throws IOException,
			IllegalArgumentException, IllegalAccessException, SQLException
	{
		Class<?> fieldType = field.getType();
		if (fieldType.isAssignableFrom(Connection.class))
			doInjection(page, field, context.connection());
		else if (fieldType.isAssignableFrom(DatabaseConnectionPool.class))
			doInjection(page, field, context.connectionPool());
		else
			throw new IOException(String.format(
				"Don't know how to inject field [%s]%s!",
				fieldType.getName(), field.getName()));
	}
	
	static public void inject(DynamicPage page, InjectionContext context)
		throws IllegalArgumentException, IOException, IllegalAccessException, SQLException
	{
		Inject inject;
		InjectCookie injectCookie;
		InjectParameter injectParameter;
		
		Class<?> cl = page.getClass();
		while (cl != null && !(cl.equals(Object.class)))
		{
			for (Field field : cl.getDeclaredFields())
			{
				inject = field.getAnnotation(Inject.class);
				if (inject != null)
					injectObject(page, field, context);
				
				injectCookie = field.getAnnotation(InjectCookie.class);
				if (injectCookie != null)
					injectCookie(page, field, context.cookies(),
						injectCookie.value());
				
				injectParameter = field.getAnnotation(InjectParameter.class);
				if (injectParameter != null)
					injectParameter(page, field,
						context.parameter(injectParameter.value()));
			}
			
			cl = cl.getSuperclass();
		}
	}
}