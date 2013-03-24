package edu.virginia.vcgr.secrun;

public interface SecureRunnableHooks
{
	static final public String CONTAINER_PRE_STARTUP = "container.pre-startup";
	static final public String CONTAINER_POST_STARTUP = "container.post-startup";

	static final public String CLIENT_PRE_STARTUP = "client.pre-startup";
	static final public String CLIENT_POST_STARTUP = "client.post-startup";
}