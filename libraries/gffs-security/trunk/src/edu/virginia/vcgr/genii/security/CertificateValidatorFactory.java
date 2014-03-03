package edu.virginia.vcgr.genii.security;

/**
 * Provides a way to separate the specifics of certificate validation (including a hidden internal
 * trust store capability) from the simple objects that just want to validate their certificates.
 */
public class CertificateValidatorFactory
{
	private static CertificateValidator validator;

	public static CertificateValidator getValidator()
	{
		return validator;
	}

	public static void setValidator(CertificateValidator newValidator)
	{
		validator = newValidator;
	}
}
