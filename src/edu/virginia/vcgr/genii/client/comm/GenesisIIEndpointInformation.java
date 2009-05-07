package edu.virginia.vcgr.genii.client.comm;

import edu.virginia.vcgr.appmgr.version.Version;

public class GenesisIIEndpointInformation
{
	private boolean _wasGenesisIIEndpoint;
	private Version _geniiEndpointVersion;
	
	public GenesisIIEndpointInformation(
		boolean wasGenesisIIEndpoint,
		Version genesisIIEndpointVersion)
	{
		_wasGenesisIIEndpoint = wasGenesisIIEndpoint;
		_geniiEndpointVersion = genesisIIEndpointVersion;
	}
	
	public final boolean wasGenesisIIEndpoint()
	{
		return _wasGenesisIIEndpoint;
	}
	
	public final Version getGenesisIIEndpointVersion()
	{
		return _geniiEndpointVersion;
	}
	
	@Override
	public String toString()
	{
		return String.format(
			"Is Genesis II Endpoint?  %s, Genesis II Version \"%s\".",
			_wasGenesisIIEndpoint, _geniiEndpointVersion == null ?
				"unknown" : _geniiEndpointVersion);
	}
}