package edu.virginia.vcgr.genii.container.cservices.accounting;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.AccountingRecordType;
import edu.virginia.vcgr.genii.security.identity.Identity;

class AccountingRecord
{
	private long _arid;
	private String _besEPI;
	private String _arch;
	private String _os;
	private String _machineName;
	private int _exitCode;
	private long _userTimeMicro;
	private long _kernelTimeMicro;
	private long _wallclockTimeMicro;
	private long _maxRSS;
	private Calendar _timestamp;

	private Collection<Identity> _credentials = new LinkedList<Identity>();
	private Vector<String> _commandLine = new Vector<String>(4);

	AccountingRecord(long arid, String besEPI, String arch, String os, String machineName, int exitCode, long userTimeMicro,
		long kernelTimeMicro, long wallclockTimeMicro, long maxRSS, Calendar timestamp)
	{
		_arid = arid;
		_besEPI = besEPI;
		_arch = arch;
		_os = os;
		_machineName = machineName;
		_exitCode = exitCode;
		_userTimeMicro = userTimeMicro;
		_kernelTimeMicro = kernelTimeMicro;
		_wallclockTimeMicro = wallclockTimeMicro;
		_maxRSS = maxRSS;
		_timestamp = timestamp;
	}

	final long recordID()
	{
		return _arid;
	}

	final void addCredential(Identity cred)
	{
		_credentials.add(cred);
	}

	final void addCommandLineElement(int index, String element)
	{
		_commandLine.setSize(index + 1);
		_commandLine.set(index, element);
	}

	final AccountingRecordType convert() throws IOException
	{
		for (int lcv = 0; lcv < _commandLine.size(); lcv++)
			if (_commandLine.get(lcv) == null)
				_commandLine.remove(lcv);

		return new AccountingRecordType(_arid, _besEPI, _arch, _os, _machineName, _commandLine.toArray(new String[_commandLine
			.size()]), _exitCode, _userTimeMicro, _kernelTimeMicro, _wallclockTimeMicro, _maxRSS, DBSerializer.serialize(
			_credentials, Long.MAX_VALUE), _timestamp);
	}
}