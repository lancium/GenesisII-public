package edu.virginia.vcgr.genii.container.cservices.infomgr;

public interface InformationListener<InformationType>
{
	public void informationUpdated(InformationEndpoint endpoint,
		InformationResult<InformationType> information);
}