package org.morgan.mnaming;

public interface MNamingContext {
	public String contextIdentifier();

	public void bind(MName name, Object value) throws MNamingException;

	public void bind(String name, Object value) throws MNamingException;

	public Object rebind(MName name, Object value) throws MNamingException;

	public Object rebind(String name, Object value) throws MNamingException;

	public Object remove(MName name) throws MNamingException;

	public Object remove(String name) throws MNamingException;

	public Object lookup(MName name) throws MNamingException;

	public Object lookup(String name) throws MNamingException;

	public <Type> Type lookup(Class<Type> type, MName name)
			throws MNamingException;

	public <Type> Type lookup(Class<Type> type, String name)
			throws MNamingException;

	public Object get(MName name) throws MNamingException;

	public Object get(String name) throws MNamingException;

	public <Type> Type get(Class<Type> type, MName name)
			throws MNamingException;

	public <Type> Type get(Class<Type> type, String name)
			throws MNamingException;

	public void clear();
}