package org.morgan.util.cache;

public interface ICacheMissResolver<KeyType, DataType>
{
	public DataType resolve(KeyType key) throws Throwable;
}