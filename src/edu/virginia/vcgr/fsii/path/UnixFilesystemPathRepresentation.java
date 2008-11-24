package edu.virginia.vcgr.fsii.path;

public class UnixFilesystemPathRepresentation 
	extends	AbstractFilesystemPathRepresentation
{
	static public UnixFilesystemPathRepresentation INSTANCE =
		new UnixFilesystemPathRepresentation();
	
	@Override
	/** {@inheritDoc} */
	protected String getRootString()
	{
		return "/";
	}

	@Override
	/** {@inheritDoc} */
	protected boolean isRooted(String path)
	{
		return path.startsWith("/");
	}

	@Override
	/** {@inheritDoc} */
	protected String[] splitPath(String path)
	{
		return path.split("/+");
	}

	@Override
	/** {@inheritDoc} */
	protected String toStringImpl(String[] path)
	{
		if (path.length == 0)
			return getRootString();
		
		StringBuilder builder = new StringBuilder();
		for (String element : path)
		{
			builder.append('/');
			builder.append(element);
		}
		
		return builder.toString();
	}
}