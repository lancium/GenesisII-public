package edu.virginia.vcgr.genii.container.cservices.infomgr;

public interface InformationPersister<InformationType>
{
	public InformationResult<InformationType> get(
		InformationEndpoint endpoint);
	public void persist(InformationEndpoint endpoint,
		InformationResult<InformationType> information);
}