package edu.virginia.vcgr.genii.client.nativeq;

public abstract class ResourceAttributes
{
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		describe(builder, "");

		return builder.toString();
	}

	protected abstract void describe(StringBuilder builder, String tabPrefix);
}