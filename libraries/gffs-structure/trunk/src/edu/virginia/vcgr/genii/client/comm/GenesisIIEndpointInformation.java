package edu.virginia.vcgr.genii.client.comm;

import edu.virginia.vcgr.appmgr.version.Version;

/**
 * an important record made about an endpoint to record whether it was a gffs endpoint or not, the version of the software there, and whether
 * it supports the newer credential shorthand.
 */
public class GenesisIIEndpointInformation
{
	private boolean _wasGenesisIIEndpoint;
	private Version _geniiEndpointVersion;
	private boolean _supportsCredentialShorthand;

	public GenesisIIEndpointInformation(boolean wasGenesisIIEndpoint, Version genesisIIEndpointVersion, boolean supportsCredentialShorthand)
	{
		_wasGenesisIIEndpoint = wasGenesisIIEndpoint;
		_geniiEndpointVersion = genesisIIEndpointVersion;
		_supportsCredentialShorthand = supportsCredentialShorthand;
	}

	public final boolean wasGenesisIIEndpoint()
	{
		return _wasGenesisIIEndpoint;
	}

	public final Version getGenesisIIEndpointVersion()
	{
		return _geniiEndpointVersion;
	}

	public final boolean supportsCredentialShorthand()
	{
		return _supportsCredentialShorthand;
	}

	@Override
	public String toString()
	{
		return String.format("Is Genesis II Endpoint? %s  GFFS Version [%s]  Credential Shorthand? %s", _wasGenesisIIEndpoint,
			_geniiEndpointVersion == null ? "unknown" : _geniiEndpointVersion, _supportsCredentialShorthand);
	}
}
