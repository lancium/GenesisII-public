package edu.virginia.vcgr.externalapp;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.jsdl.OperatingSystemNames;

@XmlRootElement(name = "external-applications")
@XmlAccessorType(XmlAccessType.NONE)
class ApplicationRegistry
{
	private Map<String, Map<ApplicationRegistrationTypes, ExternalApplication>>
		_registeredApplications = new HashMap<String, Map<ApplicationRegistrationTypes,ExternalApplication>>();
	
	@SuppressWarnings("unused")
	@XmlElement(name = "mime-type", required = false)
	private void setMimeTypeRegistration(
		MimeTypeRegistration []registrations)
	{
		for (MimeTypeRegistration reg : registrations)	
		{
			_registeredApplications.put(reg.mimeType(),
				reg.createApplicationMap());
		}
	}
	
	@SuppressWarnings("unused")
	@XmlElement(name = "default-mime-type", required = false)
	private void setDefaultRegistration(
		DefaultRegistration registration)
	{
		_registeredApplications.put(null, registration.createApplicationMap());
	}
	
	private String rootMimeType(String mimeType)
	{
		int index = mimeType.indexOf('/');
		if (index > 0)
			return mimeType.substring(0, index);
		
		return mimeType;
	}
	
	ExternalApplication getApplication(String mimeType, boolean allowDefault)
	{
		Map<ApplicationRegistrationTypes, ExternalApplication> apps =
			_registeredApplications.get(mimeType);
		if (apps == null)
			apps = _registeredApplications.get(rootMimeType(mimeType) + "/*");
		
		if (apps == null && allowDefault)
			apps = _registeredApplications.get(null);
		if (apps == null)
			return null;
		
		ExternalApplication ret = null;
		
		OperatingSystemNames myOS =
			OperatingSystemNames.getCurrentOperatingSystem();
		if (myOS.isWindows())
			ret = apps.get(ApplicationRegistrationTypes.Windows);
		else if (myOS.isLinux())
			ret = apps.get(ApplicationRegistrationTypes.Linux);
		else if (myOS.isMacOSX())
			ret = apps.get(ApplicationRegistrationTypes.MacOSX);
		
		if (ret == null)
			ret = apps.get(ApplicationRegistrationTypes.Common);
		
		return ret;
	}
}