package edu.virginia.vcgr.genii.client.utils.instantiation;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public class FromString
{
	static private Object fromString(String value, Class<?> targetType,
		Set<Class<?>> attemptedConversions)
	{
		if (targetType.equals(String.class))
			return value;
		
		if (targetType.equals(char.class) || targetType.equals(Character.class))
		{
			if (value.length() != 1)
				throw new IllegalArgumentException(String.format(
					"Cannot convert \"%s\" to a character.", value));
			
			return value.charAt(0);
		} else if (targetType.equals(short.class) || targetType.equals(Short.class))
			return Short.parseShort(value);
		else if (targetType.equals(int.class) || targetType.equals(Integer.class))
			return Integer.parseInt(value);
		else if (targetType.equals(long.class) || targetType.equals(Long.class))
			return Long.parseLong(value);
		else if (targetType.equals(float.class) || targetType.equals(Float.class))
			return Float.parseFloat(value);
		else if (targetType.equals(double.class) || targetType.equals(Double.class))
			return Double.parseDouble(value);
		else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class))
			return Boolean.parseBoolean(value);
		else if (targetType.equals(StringBuilder.class))
			return new StringBuilder(value);
		else if (targetType.equals(StringBuffer.class))
			return new StringBuffer(value);
		else if (targetType.isAssignableFrom(StringReader.class))
			return new StringReader(value);
		else if (targetType.isAssignableFrom(ByteArrayInputStream.class))
			return new ByteArrayInputStream(value.getBytes());
		
		try
		{
			Constructor<?> cons = targetType.getConstructor(String.class);
			return cons.newInstance(value);
		} 
		catch (Throwable cause)
		{
			// Ignore
		}
		
		for (Constructor<?> cons : targetType.getConstructors())
		{
			if (cons.getParameterTypes().length == 1)
			{
				Class<?> paramType = cons.getParameterTypes()[0];
				if (attemptedConversions.contains(paramType))
					continue;
				
				attemptedConversions.add(paramType);
				try
				{
					return fromString(value, paramType, attemptedConversions);
				}
				catch (Throwable cause)
				{
					// Ignore
				}
			}
		}
		
		throw new IllegalArgumentException(String.format(
			"Don't know how to create a %s from a String.",
			targetType));
	}
	
	static public Object fromString(String value, Class<?> targetType)
	{
		Set<Class<?>> attemptedConversions = new HashSet<Class<?>>();
		attemptedConversions.add(targetType);
		return fromString(value, targetType, attemptedConversions);
	}
}