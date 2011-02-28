package edu.virginia.vcgr.genii.client.nativeq;

import java.io.Serializable;
import java.util.List;

public interface JobToken extends Serializable
{
	public List<String> getCmdLine();
}