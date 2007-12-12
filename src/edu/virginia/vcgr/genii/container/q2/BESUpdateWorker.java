package edu.virginia.vcgr.genii.container.q2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;

public class BESUpdateWorker implements Runnable
{
	static private Log _logger = LogFactory.getLog(BESUpdateWorker.class);
	
	private BESManager _manager;
	private long _besID;
	private BESPortType _clientStub;
	
	public BESUpdateWorker(BESManager manager, long besID, BESPortType clientStub)
	{
		_manager = manager;
		_besID = besID;
		_clientStub = clientStub;
	}
	
	public void run()
	{
		try
		{
			GetFactoryAttributesDocumentResponseType resp =
				_clientStub.getFactoryAttributesDocument(
					new GetFactoryAttributesDocumentType());
			if (resp.getFactoryResourceAttributesDocument(
				).isIsAcceptingNewActivities())
			{
				_manager.markBESAsAvailable(_besID);
			} else
			{
				_manager.markBESAsUnavailable(_besID);
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to update BES container " + _besID, cause);
			_manager.markBESAsUnavailable(_besID);
		}
	}
}