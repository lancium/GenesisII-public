package edu.virginia.vcgr.fsii.path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsFilesystemPathRepresentation
	extends AbstractFilesystemPathRepresentation
{
	static private final Pattern WINDOWS_ROOT_PATTERN =
		Pattern.compile("^(?:[a-zA-Z]:)?\\\\+(.*)$");
	
	static public WindowsFilesystemPathRepresentation DRIVELESS_INSTANCE =
		new WindowsFilesystemPathRepresentation();
	
	private Character _driveLetter = null;
	
	/**
	 * Initializes the WindowsFilesystemPathRepresentation with a drive
	 * letter (used only in toString transformations).
	 * 
	 * @param driveLetter The drive letter to use for toString transformations.
	 */
	protected WindowsFilesystemPathRepresentation(Character driveLetter)
	{
		_driveLetter = driveLetter;
	}
	
	/**
	 * Initializes the WindowsFilesystemPathRepresentation with a drive
	 * letter (used only in toString transformations).
	 * 
	 * @param driveLetter The drive letter to use for toString transformations.
	 */
	public WindowsFilesystemPathRepresentation(char driveLetter)
	{
		this(new Character(driveLetter));
	}
	
	/**
	 * Initializes the WindowsFilesystemPathRepresentation without a drive
	 * letter.  In this case, toString transformations will simply be rooted
	 * using a backslash.
	 */
	public WindowsFilesystemPathRepresentation()
	{
		this(null);
	}
	
	@Override
	/** {@inheritDoc} */
	protected String getRootString()
	{
		if (_driveLetter != null)
			return String.format("%c:\\", _driveLetter.charValue());
		
		return "\\";
	}

	@Override
	/** {@inheritDoc} */
	protected boolean isRooted(String path)
	{
		return WINDOWS_ROOT_PATTERN.matcher(path).matches();
	}

	@Override
	/** {@inheritDoc} */
	protected String[] splitPath(String path)
	{
		Matcher m = WINDOWS_ROOT_PATTERN.matcher(path);
		if (m.matches())
			path = m.group(1);
		
		return path.split("\\\\+");
	}

	@Override
	/** {@inheritDoc} */
	protected String toStringImpl(String[] path)
	{
		if (path.length == 0)
			return getRootString();
		
		StringBuilder builder = new StringBuilder(
			_driveLetter == null ? 
				"" : String.format("%c:", _driveLetter.charValue()));
		for (String element : path)
		{
			builder.append('\\');
			builder.append(element);
		}
		
		return builder.toString();
	}
}