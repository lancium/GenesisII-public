package edu.virginia.vcgr.appmgr.patch;

import java.io.File;
import java.io.IOException;

public class Permissions
{
	static private void verifySetReadable(File target, boolean readable, boolean ownerOnly) throws IOException
	{
		if (!target.setReadable(readable, ownerOnly))
			throw new IOException(String.format("Unable to set readable permission on \"%s\".", target));
	}

	static private void verifySetWritable(File target, boolean writable, boolean ownerOnly) throws IOException
	{
		if (!target.setWritable(writable, ownerOnly))
			throw new IOException(String.format("Unable to set writable permission on \"%s\".", target));
	}

	static private void verifySetExecutable(File target, boolean executable, boolean ownerOnly) throws IOException
	{
		if (!target.setExecutable(executable, ownerOnly))
			throw new IOException(String.format("Unable to set executable permission on \"%s\".", target));
	}

	static private boolean getPermission(String string, int index, char desired) throws IOException
	{
		char c = string.charAt(index);
		if (c == desired)
			return true;
		else if (c == '-')
			return false;

		throw new IOException(String.format("Unable to parse permission string \"%s\".", string));
	}

	private boolean _ownerCanRead;
	private boolean _ownerCanWrite;
	private boolean _ownerCanExecute;
	private boolean _groupCanRead;
	private boolean _groupCanWrite;
	private boolean _groupCanExecute;

	private File _target;

	public Permissions(File target)
	{
		_target = target;

		if (_target.exists()) {
			_ownerCanRead = _target.canRead();
			_ownerCanWrite = _target.canWrite();
			_ownerCanExecute = _target.canExecute();
			_groupCanRead = true;
			_groupCanWrite = false;
			_groupCanExecute = false;
		} else {
			_ownerCanRead = true;
			_ownerCanWrite = true;
			_ownerCanExecute = false;
			_groupCanRead = true;
			_groupCanWrite = false;
			_groupCanExecute = false;
		}
	}

	public void override(String permissionString) throws IOException
	{
		if (permissionString != null)
			permissionString = permissionString.trim();
		if (permissionString.length() > 0) {
			if (permissionString.length() != 6)
				throw new IOException(String.format("Cannot parse permission string \"%s\".", permissionString));

			_ownerCanRead = getPermission(permissionString, 0, 'r');
			_ownerCanWrite = getPermission(permissionString, 1, 'w');
			_ownerCanExecute = getPermission(permissionString, 2, 'x');
			_groupCanRead = getPermission(permissionString, 3, 'r');
			_groupCanWrite = getPermission(permissionString, 4, 'w');
			_groupCanExecute = getPermission(permissionString, 5, 'x');
		}
	}

	public void enforce() throws IOException
	{
		verifySetReadable(_target, false, false);
		verifySetWritable(_target, false, false);
		verifySetExecutable(_target, false, false);

		verifySetReadable(_target, _ownerCanRead, true);
		verifySetReadable(_target, _ownerCanRead, !_groupCanRead);
		verifySetWritable(_target, _ownerCanWrite, true);
		verifySetWritable(_target, _ownerCanWrite, !_groupCanWrite);
		verifySetExecutable(_target, _ownerCanExecute, true);
		verifySetExecutable(_target, _ownerCanExecute, !_groupCanExecute);
	}
}