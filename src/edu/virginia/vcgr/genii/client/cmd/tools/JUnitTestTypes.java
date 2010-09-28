package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.test.installation.RootInstallationTestSuite;
import edu.virginia.vcgr.test.regression.RootRegressionTestSuite;

public enum JUnitTestTypes
{
	Regression(RootRegressionTestSuite.class),
	Installation(RootInstallationTestSuite.class);
	
	private Class<?> []_classes;
	
	private JUnitTestTypes(Class<?>...classes)
	{
		_classes = classes;
	}
	
	public Class<?>[] classes()
	{
		return _classes;
	}
	
	public boolean isValid()
	{
		return _classes.length > 0;
	}
}