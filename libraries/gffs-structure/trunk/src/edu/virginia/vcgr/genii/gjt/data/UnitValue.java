package edu.virginia.vcgr.genii.gjt.data;

public interface UnitValue<V, U extends Enum<U>>
{
	public V value();

	public void value(V value);

	public U units();

	public void units(U units);
}