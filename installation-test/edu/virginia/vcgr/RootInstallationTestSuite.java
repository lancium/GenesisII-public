package edu.virginia.vcgr;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.virginia.vcgr.client.ClientPackageTestSuite;
import edu.virginia.vcgr.genii.container.ContainerPackageTestSuite;

@RunWith(Suite.class)
@SuiteClasses({ContainerPackageTestSuite.class, ClientPackageTestSuite.class})
public class RootInstallationTestSuite
{
}