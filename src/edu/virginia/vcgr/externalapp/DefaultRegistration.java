package edu.virginia.vcgr.externalapp;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class DefaultRegistration
{
	static private Log _logger = LogFactory.getLog(DefaultRegistration.class);
	
	@XmlElement(name = "application-registration", required = false, nillable = true)
	private Collection<ApplicationRegistration> _registrations = 
		new LinkedList<ApplicationRegistration>();
	
	final Map<ApplicationRegistrationTypes, ExternalApplication> 
		createApplicationMap()
	{
		Map<ApplicationRegistrationTypes, ExternalApplication> ret =
			new EnumMap<ApplicationRegistrationTypes, ExternalApplication>(
				ApplicationRegistrationTypes.class);
		
		for (ApplicationRegistration reg : _registrations)
		{
			try
			{
				ret.put(reg.registrationType(), reg.createApplication());
			}
			catch (Throwable cause)
			{
				_logger.warn(String.format(
					"Unable to create external application for registration type %s.",
					reg.registrationType()), cause);
			}
		}
		
		return ret;
	}
}