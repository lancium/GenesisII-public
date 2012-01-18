package edu.virginia.vcgr.genii.container.common.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class ConstructionParametersFork extends AbstractStreamableByteIOFactoryResourceFork
{
	public ConstructionParametersFork(ResourceForkService service,
		String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		try
		{
			ConstructionParameters cParams =
				ConstructionParameters.deserializeConstructionParameters(
					getService().getClass(), source);
			ResourceManager.getCurrentResource().dereference(
				).constructionParameters(cParams);
		}
		catch (JAXBException e)
		{
			throw new IOException(
				"Unable to unmarshall new construction parameters.", e);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void snapshotState(OutputStream sink) throws IOException
	{
		try
		{
			ConstructionParameters cParams = 
				ResourceManager.getCurrentResource().dereference(
					).constructionParameters(getService().getClass());
			cParams.serialize(sink);
		}
		catch (JAXBException e)
		{
			throw new IOException(
				"Unable to marshall construction parameters.", e);
		}
	}
}