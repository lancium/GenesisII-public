package edu.virginia.vcgr.genii.ui.persist;

import java.io.IOException;
import java.io.ObjectOutputStream;

public interface Persistable
{
	public boolean persist(ObjectOutputStream oos) throws IOException;
}