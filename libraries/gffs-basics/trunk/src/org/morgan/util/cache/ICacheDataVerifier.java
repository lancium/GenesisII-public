package org.morgan.util.cache;

public interface ICacheDataVerifier<KeyType, DataType>
{
	public boolean isValid(KeyType key, DataType data);
}