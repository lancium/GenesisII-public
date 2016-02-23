package edu.virginia.vcgr.genii.client.comm;

import edu.virginia.vcgr.appmgr.version.Version;

/**
 * an important record made about an endpoint to record whether it was a gffs endpoint or not, the version of the software there, and whether
 * it supports the newer credential streamlining.
 */
public class GenesisIIEndpointInformation
{
	private boolean _wasGenesisIIEndpoint;
	private Version _geniiEndpointVersion;
	private boolean _supportsCredentialStreamlining;

	public GenesisIIEndpointInformation(boolean wasGenesisIIEndpoint, Version genesisIIEndpointVersion,
		boolean supportsCredentialStreamlining)
	{
		_wasGenesisIIEndpoint = wasGenesisIIEndpoint;
		_geniiEndpointVersion = genesisIIEndpointVersion;
		_supportsCredentialStreamlining = supportsCredentialStreamlining;
	}

	public final boolean wasGenesisIIEndpoint()
	{
		return _wasGenesisIIEndpoint;
	}

	public final Version getGenesisIIEndpointVersion()
	{
		return _geniiEndpointVersion;
	}

	public final boolean supportsCredentialStreamlining()
	{
		return _supportsCredentialStreamlining;
	}

	@Override
	public String toString()
	{
		return String.format("Is Genesis II Endpoint? %s  GFFS Version [%s]  Credential Streamlining? %s", _wasGenesisIIEndpoint,
			_geniiEndpointVersion == null ? "unknown" : _geniiEndpointVersion, _supportsCredentialStreamlining);
	}
}
