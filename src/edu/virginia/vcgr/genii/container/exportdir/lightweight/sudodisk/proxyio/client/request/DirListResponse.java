package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.DirListing;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.StatAttributes;

public class DirListResponse
{

	private int _errorCode;
	private String _errorMsg;
	private DirListing _listing;

	public DirListResponse(int errorCode, DirListing listing)
	{
		_errorCode = errorCode;
		_listing = listing;
	}

	public DirListResponse(int errorCode, String errorMsg)
	{
		_errorCode = errorCode;
		_errorMsg = errorMsg;
	}

	public String getErrorMsg()
	{
		return _errorMsg;
	}

	public void disp()
	{
		if (_listing == null || _errorCode != 0) {
			System.out.println("----------------------");
			System.out.println("----------------------");
			return;
		}

		System.out.println("----------------------");
		for (StatAttributes sa : _listing.getDirListing()) {
			System.out.println(sa);
		}
		System.out.println("----------------------");
	}

	public int getErrorCode()
	{
		return _errorCode;
	}

	public DirListing getListing()
	{
		return _listing;
	}
}
