package edu.virginia.vcgr.genii.container.cservices;

import java.io.Serializable;

public interface ContainerServicePropertyListener
{
	public void propertyChanged(String propertyName, Serializable newValue);
}